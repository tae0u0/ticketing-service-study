package com.ticketing.booking.dto;

import com.ticketing.booking.domain.Booking;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookingResponse {
    private final Long bookingId;
    private final String bookingNumber;
    private final Long userId;
    private final Long seatId;
    private final Long eventId;
    private final String status;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;

    private BookingResponse(Booking b) {
        this.bookingId = b.getId();
        this.bookingNumber = b.getBookingNumber();
        this.userId = b.getUserId();
        this.seatId = b.getSeatId();
        this.eventId = b.getEventId();
        this.status = b.getStatus().name();
        this.cancelledAt = b.getCancelledAt();
        this.createdAt = b.getCreatedAt();
    }

    public static BookingResponse of(Booking b) {
        return new BookingResponse(b);
    }
}
