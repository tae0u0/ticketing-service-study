package com.ticketing.reservation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.common.event.SeatHeldEvent;
import com.ticketing.common.event.SeatHoldExpiredEvent;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.exception.ErrorCode;
import com.ticketing.reservation.domain.OutboxEvent;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.dto.HoldSeatRequest;
import com.ticketing.reservation.dto.ReservationResponse;
import com.ticketing.reservation.lock.DistributedLockService;
import com.ticketing.reservation.lock.DistributedLockService.LockAcquisitionException;
import com.ticketing.reservation.repository.OutboxEventRepository;
import com.ticketing.reservation.repository.ReservationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final DistributedLockService lockService;
    private final ObjectMapper objectMapper;
    private final Counter holdSuccessCounter;
    private final Counter holdFailureCounter;

    private static final int HOLD_MINUTES = 5;

    public ReservationService(ReservationRepository reservationRepository,
                               OutboxEventRepository outboxEventRepository,
                               DistributedLockService lockService,
                               ObjectMapper objectMapper,
                               MeterRegistry meterRegistry) {
        this.reservationRepository = reservationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.lockService = lockService;
        this.objectMapper = objectMapper;
        this.holdSuccessCounter = Counter.builder("ticketing.seat.hold.success")
                .description("좌석 선점 성공 건수")
                .register(meterRegistry);
        this.holdFailureCounter = Counter.builder("ticketing.seat.hold.failure")
                .description("좌석 선점 실패 건수")
                .register(meterRegistry);
    }

    /**
     * 좌석 임시 선점
     *
     * 처리 순서:
     * 1. Idempotency 키 중복 확인 (동일 요청 재처리 방지)
     * 2. Redis 분산 락 획득 (동시 선점 방지 1차 방어)
     * 3. 현재 HELD 상태 좌석 여부 확인 (2차 방어)
     * 4. Reservation INSERT + OutboxEvent INSERT (같은 트랜잭션)
     * 5. OutboxPublisher가 비동기로 Kafka 발행
     */
    public ReservationResponse holdSeat(Long userId, HoldSeatRequest request) {
        // 멱등성: 동일 idempotencyKey 요청은 기존 결과 반환
        return reservationRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(existing -> {
                    log.info("중복 선점 요청 반환 기존결과 reservationId={}", existing.getId());
                    return ReservationResponse.of(existing);
                })
                .orElseGet(() -> executeHoldWithLock(userId, request));
    }

    private ReservationResponse executeHoldWithLock(Long userId, HoldSeatRequest request) {
        try {
            return lockService.executeWithSeatLock(request.getSeatId(), () ->
                    doHoldSeat(userId, request)
            );
        } catch (LockAcquisitionException e) {
            holdFailureCounter.increment();
            throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
        }
    }

    @Transactional
    protected ReservationResponse doHoldSeat(Long userId, HoldSeatRequest request) {
        // 현재 HELD 상태인 예약이 있는지 확인 (Redis 락 뚫릴 경우 2차 방어)
        reservationRepository.findBySeatIdAndStatus(request.getSeatId(), ReservationStatus.HELD)
                .ifPresent(existing -> {
                    holdFailureCounter.increment();
                    throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
                });

        Reservation reservation = Reservation.create(
                userId, request.getSeatId(), request.getEventId(),
                request.getIdempotencyKey(), HOLD_MINUTES
        );
        reservationRepository.save(reservation);

        // Outbox 이벤트 저장 (같은 트랜잭션 → 원자성 보장)
        SeatHeldEvent event = new SeatHeldEvent(
                reservation.getId(), userId, request.getSeatId(),
                request.getEventId(),
                reservation.getExpiresAt().toInstant(java.time.ZoneOffset.UTC)
        );
        saveOutboxEvent("Reservation", reservation.getId(), event);

        holdSuccessCounter.increment();
        log.info("좌석 선점 성공 reservationId={} seatId={} userId={}", reservation.getId(), request.getSeatId(), userId);
        return ReservationResponse.of(reservation);
    }

    /**
     * 예약 만료 처리 (스케줄러에서 호출)
     * Reservation.status = EXPIRED + OutboxEvent(SeatHoldExpired) 저장
     */
    @Transactional
    public void expireReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.HELD) return;

        reservation.expire();

        SeatHoldExpiredEvent event = new SeatHoldExpiredEvent(
                reservation.getId(), reservation.getUserId(),
                reservation.getSeatId(), reservation.getEventId()
        );
        // domainEventId = 공연 ID
        saveOutboxEvent("Reservation", reservation.getId(), event);
        log.info("예약 만료 처리 reservationId={} seatId={}", reservationId, reservation.getSeatId());
    }

    /**
     * 결제 서비스에서 CONFIRMED 상태로 변경할 때 호출
     */
    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.isExpired()) {
            reservation.expire();
            throw new BusinessException(ErrorCode.RESERVATION_EXPIRED);
        }
        reservation.confirm();
        log.info("예약 확정 reservationId={}", reservationId);
    }

    /**
     * BookingConfirmedEvent 수신 시 seatId로 HELD 예약을 CONFIRMED 상태로 변경
     * 만료 스케줄러가 확정된 예약을 실수로 만료시키지 않도록 보호
     */
    @Transactional
    public void confirmReservationByBooking(Long bookingId, Long seatId) {
        reservationRepository.findBySeatIdAndStatus(seatId, ReservationStatus.HELD)
                .ifPresent(reservation -> {
                    reservation.confirm();
                    log.info("예약 확정 (booking) seatId={} reservationId={}", seatId, reservation.getId());
                });
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReservationResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return ReservationResponse.of(reservation);
    }

    private void saveOutboxEvent(String aggregateType, Long aggregateId, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(
                    OutboxEvent.create(aggregateType, aggregateId, event.getClass().getSimpleName(), payload)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 이벤트 직렬화 실패", e);
        }
    }
}
