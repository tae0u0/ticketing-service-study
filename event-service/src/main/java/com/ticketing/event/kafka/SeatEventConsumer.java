package com.ticketing.event.kafka;

import com.ticketing.common.event.*;
import com.ticketing.event.service.SeatService;
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
public class SeatEventConsumer {

    private final SeatService seatService;

    @KafkaListener(
            topics = "reservation.events",
            groupId = "event-service-reservation",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReservationEvent(@Payload DomainEvent event,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("이벤트 수신 type={} topic={}", event.getEventType(), topic);
        try {
            if (event instanceof SeatHeldEvent e) {
                seatService.holdSeat(e.getSeatId());
            } else if (event instanceof SeatHoldExpiredEvent e) {
                seatService.releaseSeat(e.getSeatId());
            }
        } catch (Exception ex) {
            log.error("좌석 상태 업데이트 실패 eventType={} error={}", event.getEventType(), ex.getMessage());
        }
    }

    @KafkaListener(
            topics = "booking.events",
            groupId = "event-service-booking",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBookingEvent(@Payload DomainEvent event) {
        log.info("예매 이벤트 수신 type={}", event.getEventType());
        try {
            if (event instanceof BookingConfirmedEvent e) {
                seatService.bookSeat(e.getSeatId());
            } else if (event instanceof BookingCancelledEvent e) {
                seatService.cancelSeat(e.getSeatId());
            }
        } catch (Exception ex) {
            log.error("좌석 상태 업데이트 실패 eventType={} error={}", event.getEventType(), ex.getMessage());
        }
    }
}
