package com.ticketing.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.booking.domain.*;
import com.ticketing.booking.dto.BookingResponse;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.booking.repository.OutboxEventRepository;
import com.ticketing.booking.repository.ProcessedEventRepository;
import com.ticketing.common.event.BookingCancelledEvent;
import com.ticketing.common.event.BookingConfirmedEvent;
import com.ticketing.common.event.PaymentFailedEvent;
import com.ticketing.common.event.PaymentSucceededEvent;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * PaymentSucceeded 이벤트 수신 → 예매 확정
     *
     * 멱등성 보장:
     * - ProcessedEvent 테이블에 eventId 기록
     * - 비즈니스 로직 + ProcessedEvent 저장이 같은 트랜잭션
     * - 재소비 시 ProcessedEvent 조회로 중복 처리 방지
     */
    @Transactional
    public void confirmBooking(PaymentSucceededEvent event) {
        // 멱등성 체크
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("이미 처리된 이벤트 skip eventId={}", event.getEventId());
            return;
        }

        // 이미 동일 예약으로 예매가 존재하는지 확인
        if (bookingRepository.findByReservationId(event.getReservationId()).isPresent()) {
            log.warn("이미 확정된 예매 reservationId={}", event.getReservationId());
            processedEventRepository.save(ProcessedEvent.of(event.getEventId()));
            return;
        }

        Booking booking = Booking.create(
                event.getReservationId(), event.getPaymentId(),
                event.getUserId(), event.getSeatId(), event.getDomainEventId()
        );
        bookingRepository.save(booking);

        BookingConfirmedEvent confirmedEvent = new BookingConfirmedEvent(
                booking.getId(), booking.getBookingNumber(),
                event.getUserId(), event.getSeatId(), event.getDomainEventId(), event.getAmount()
        );
        saveOutboxEvent("Booking", booking.getId(), confirmedEvent);

        // 처리 완료 기록 (같은 트랜잭션)
        processedEventRepository.save(ProcessedEvent.of(event.getEventId()));

        log.info("예매 확정 완료 bookingId={} bookingNumber={}", booking.getId(), booking.getBookingNumber());
    }

    /**
     * PaymentFailed 이벤트 수신 → 좌석 해제 트리거
     * (실제 좌석 해제는 Event Service가 SeatHoldExpired 이벤트를 처리)
     */
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("이미 처리된 이벤트 skip eventId={}", event.getEventId());
            return;
        }
        // 결제 실패 시 별도 처리 없음 - Reservation Service가 만료 처리
        processedEventRepository.save(ProcessedEvent.of(event.getEventId()));
        log.info("결제 실패 이벤트 처리 paymentId={} reason={}", event.getPaymentId(), event.getFailureReason());
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        booking.cancel();

        // 취소 이벤트 Outbox 저장
        BookingCancelledEvent cancelledEvent = new BookingCancelledEvent(
                booking.getId(), userId, booking.getSeatId(), booking.getEventId(), 0  // refundAmount: Payment Service에서 별도 환불 처리
        );
        saveOutboxEvent("Booking", booking.getId(), cancelledEvent);

        log.info("예매 취소 완료 bookingId={}", bookingId);
        return BookingResponse.of(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookingResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return BookingResponse.of(booking);
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
