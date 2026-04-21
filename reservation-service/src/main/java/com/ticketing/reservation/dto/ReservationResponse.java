package com.ticketing.reservation.dto;

import com.ticketing.reservation.domain.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class ReservationResponse {
    private final Long reservationId;
    private final Long userId;
    private final Long seatId;
    private final Long eventId;
    private final String status;
    private final LocalDateTime expiresAt;
    private final long remainingSeconds;

    private ReservationResponse(Reservation r) {
        this.reservationId = r.getId();
        this.userId = r.getUserId();
        this.seatId = r.getSeatId();
        this.eventId = r.getEventId();
        this.status = r.getStatus().name();
        this.expiresAt = r.getExpiresAt();
        this.remainingSeconds = Math.max(0,
                ChronoUnit.SECONDS.between(LocalDateTime.now(), r.getExpiresAt()));
    }

    public static ReservationResponse of(Reservation r) {
        return new ReservationResponse(r);
    }
}
