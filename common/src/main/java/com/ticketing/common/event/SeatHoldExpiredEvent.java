package com.ticketing.common.event;

public class SeatHoldExpiredEvent extends DomainEvent {

    private Long reservationId;
    private Long userId;
    private Long seatId;
    private Long domainEventId;

    public SeatHoldExpiredEvent() {
        super("SeatHoldExpired");
    }

    public SeatHoldExpiredEvent(Long reservationId, Long userId, Long seatId, Long domainEventId) {
        super("SeatHoldExpired");
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.domainEventId = domainEventId;
    }

    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Long getSeatId() { return seatId; }
    public Long getDomainEventId() { return domainEventId; }
}
