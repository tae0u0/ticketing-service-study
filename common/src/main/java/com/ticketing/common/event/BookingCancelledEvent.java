package com.ticketing.common.event;

public class BookingCancelledEvent extends DomainEvent {

    private Long bookingId;
    private Long userId;
    private Long seatId;
    private Long domainEventId;
    private int refundAmount;

    public BookingCancelledEvent() {
        super("BookingCancelled");
    }

    public BookingCancelledEvent(Long bookingId, Long userId, Long seatId, Long domainEventId, int refundAmount) {
        super("BookingCancelled");
        this.bookingId = bookingId;
        this.userId = userId;
        this.seatId = seatId;
        this.domainEventId = domainEventId;
        this.refundAmount = refundAmount;
    }

    public Long getBookingId() { return bookingId; }
    public Long getUserId() { return userId; }
    public Long getSeatId() { return seatId; }
    public Long getDomainEventId() { return domainEventId; }
    public int getRefundAmount() { return refundAmount; }
}
