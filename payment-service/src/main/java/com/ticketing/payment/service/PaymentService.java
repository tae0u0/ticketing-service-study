package com.ticketing.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.common.event.PaymentFailedEvent;
import com.ticketing.common.event.PaymentSucceededEvent;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.exception.ErrorCode;
import com.ticketing.payment.domain.OutboxEvent;
import com.ticketing.payment.domain.Payment;
import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.dto.PaymentRequest;
import com.ticketing.payment.dto.PaymentResponse;
import com.ticketing.payment.pg.MockPGService;
import com.ticketing.payment.repository.OutboxEventRepository;
import com.ticketing.payment.repository.PaymentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final MockPGService pgService;
    private final ObjectMapper objectMapper;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;

    /**
     * 결제 요청 처리
     *
     * 처리 순서:
     * 1. Idempotency 키 중복 확인
     * 2. Payment INSERT (PENDING 상태)
     * 3. PG사 결제 요청
     * 4. 결과에 따라 Payment 상태 업데이트 + Outbox 저장 (같은 트랜잭션)
     *
     * PG 호출을 트랜잭션 외부에서 할 수도 있지만 여기서는 단순화.
     * 실무에서는 PG 호출 전에 DB 저장, 응답 후 상태 업데이트로 분리.
     */
    @Transactional
    public PaymentResponse pay(Long userId, PaymentRequest request) {
        // 멱등성: 같은 idempotencyKey는 기존 결과 반환
        return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(existing -> {
                    log.info("중복 결제 요청 기존 결과 반환 paymentId={}", existing.getId());
                    return PaymentResponse.of(existing);
                })
                .orElseGet(() -> processNewPayment(userId, request));
    }

    private PaymentResponse processNewPayment(Long userId, PaymentRequest request) {
        Payment payment = Payment.create(
                request.getReservationId(), userId,
                request.getSeatId(), request.getEventId(),
                request.getAmount(), request.getPaymentMethod(),
                request.getIdempotencyKey()
        );
        paymentRepository.save(payment);

        // PG 결제 요청
        MockPGService.PGResult pgResult = pgService.requestPayment(
                payment.getId(), request.getAmount(), request.getPaymentMethod());

        if (pgResult.success()) {
            payment.succeed(pgResult.pgTransactionId());
            saveOutboxEvent("Payment", payment.getId(),
                    new PaymentSucceededEvent(payment.getId(), payment.getReservationId(),
                            userId, payment.getSeatId(), payment.getEventId(),  // eventId = domainEventId
                            payment.getAmount(), pgResult.pgTransactionId()));
            paymentSuccessCounter.increment();
            log.info("결제 성공 paymentId={} pgTxId={}", payment.getId(), pgResult.pgTransactionId());
        } else {
            payment.fail(pgResult.failureReason());
            saveOutboxEvent("Payment", payment.getId(),
                    new PaymentFailedEvent(payment.getId(), payment.getReservationId(),
                            userId, payment.getSeatId(), payment.getEventId(),  // eventId = domainEventId
                            pgResult.failureReason()));
            paymentFailureCounter.increment();
            log.warn("결제 실패 paymentId={} reason={}", payment.getId(), pgResult.failureReason());
        }

        return PaymentResponse.of(payment);
    }

    @Transactional
    public PaymentResponse refund(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "성공한 결제만 환불 가능합니다.");
        }

        pgService.requestRefund(payment.getPgTransactionId(), payment.getAmount());
        payment.refund();
        log.info("환불 처리 완료 paymentId={}", paymentId);
        return PaymentResponse.of(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponse.of(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(PaymentResponse::of)
                .collect(Collectors.toList());
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
