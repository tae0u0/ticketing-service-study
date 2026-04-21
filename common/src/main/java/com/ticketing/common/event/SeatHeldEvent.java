package com.ticketing.common.event;

import java.time.Instant;

public class SeatHeldEvent extends DomainEvent {

    private Long reservationId;
    private Long userId;
    private Long seatId;
    private Long domainEventId;   // 공연 ID (eventId 충돌 방지)
    private Instant expiresAt;

    public SeatHeldEvent() {
        super("SeatHeld");
    }

    public SeatHeldEvent(Long reservationId, Long userId, Long seatId, Long domainEventId, Instant expiresAt) {
        super("SeatHeld");
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.domainEventId = domainEventId;
        this.expiresAt = expiresAt;
    }

    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Long getSeatId() { return seatId; }
    public Long getDomainEventId() { return domainEventId; }
    public Instant getExpiresAt() { return expiresAt; }
}
