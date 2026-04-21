package com.ticketing.common.event;

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

    public Long getBookingId() { return bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public Long getUserId() { return userId; }
    public Long getSeatId() { return seatId; }
    public Long getDomainEventId() { return domainEventId; }
    public int getAmount() { return amount; }
}
