package com.ticketing.reservation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static Reservation create(Long userId, Long seatId, Long eventId,
                                      String idempotencyKey, int holdMinutes) {
        Reservation r = new Reservation();
        r.userId = userId;
        r.seatId = seatId;
        r.eventId = eventId;
        r.status = ReservationStatus.HELD;
        r.expiresAt = LocalDateTime.now().plusMinutes(holdMinutes);
        r.idempotencyKey = idempotencyKey;
        return r;
    }

    public void expire() {
        if (this.status == ReservationStatus.HELD) {
            this.status = ReservationStatus.EXPIRED;
        }
    }

    public void confirm() {
        if (this.status != ReservationStatus.HELD) {
            throw new IllegalStateException("HELD 상태의 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isExpired() {
        return this.status == ReservationStatus.HELD
                && LocalDateTime.now().isAfter(this.expiresAt);
    }
}
