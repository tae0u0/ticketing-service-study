package com.ticketing.common.event;

import lombok.Getter;

@Getter
public class BookingConfirmedEvent extends DomainEvent {

    private Long bookingId;
    private String bookingNumber;
    private Long userId;
    private Long seatId;
    private Long domainEventId;
    private int amount;

    public BookingConfirmedEvent() {
        super("BookingConfirmed");
    }

    public BookingConfirmedEvent(Long bookingId, String bookingNumber, Long userId,
                                  Long seatId, Long domainEventId, int amount) {
        super("BookingConfirmed");
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.userId = userId;
        this.seatId = seatId;
        this.domainEventId = domainEventId;
        this.amount = amount;
    }

}
