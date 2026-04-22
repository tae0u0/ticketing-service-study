package com.ticketing.reservation.kafka;

import com.ticketing.common.event.BookingConfirmedEvent;
import com.ticketing.common.event.DomainEvent;
import com.ticketing.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final ReservationService reservationService;

    @KafkaListener(
            topics = "booking.events",
            groupId = "reservation-service-booking",
            containerFactory = "reservationKafkaListenerContainerFactory"
    )
    public void handleBookingEvent(@Payload DomainEvent event) {
        log.info("예매 이벤트 수신 type={}", event.getEventType());
        try {
            if (event instanceof BookingConfirmedEvent e) {
                reservationService.confirmReservationByBooking(e.getBookingId(), e.getSeatId());
            }
        } catch (Exception ex) {
            log.error("예약 확정 처리 실패 eventType={} error={}", event.getEventType(), ex.getMessage());
        }
    }
}
