package com.ticketing.booking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.booking.service.BookingService;
import com.ticketing.common.event.DomainEvent;
import com.ticketing.common.event.PaymentFailedEvent;
import com.ticketing.common.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final BookingService bookingService;

    /**
     * payment.events 토픽을 소비한다.
     *
     * 멱등성:
     * - BookingService 내부에서 ProcessedEvent 테이블로 중복 처리 방지
     * - @Transactional + ProcessedEvent 저장이 같은 트랜잭션
     *
     * 재처리:
     * - 처리 실패 시 DefaultErrorHandler가 3회 재시도
     * - 3회 실패 시 payment.events.DLQ 토픽으로 이동
     */
    @KafkaListener(
            topics = "payment.events",
            groupId = "booking-service-payment",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(@Payload DomainEvent event,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("결제 이벤트 수신 type={} eventId={} partition={} offset={}",
                event.getEventType(), event.getEventId(), partition, offset);

        try {
            if (event instanceof PaymentSucceededEvent e) {
                bookingService.confirmBooking(e);
            } else if (event instanceof PaymentFailedEvent e) {
                bookingService.handlePaymentFailed(e);
            }
        } catch (Exception ex) {
            log.error("결제 이벤트 처리 실패 eventId={} error={}", event.getEventId(), ex.getMessage());
            throw ex; // 재시도를 위해 예외 전파
        }
    }
}
