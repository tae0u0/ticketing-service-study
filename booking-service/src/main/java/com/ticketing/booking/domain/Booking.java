package com.ticketing.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long reservationId;

    @Column(nullable = false, unique = true)
    private Long paymentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, unique = true)
    private String bookingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = BookingStatus.CONFIRMED;
    }

    public static Booking create(Long reservationId, Long paymentId, Long userId,
                                  Long seatId, Long eventId) {
        Booking b = new Booking();
        b.reservationId = reservationId;
        b.paymentId = paymentId;
        b.userId = userId;
        b.seatId = seatId;
        b.eventId = eventId;
        b.bookingNumber = generateBookingNumber();
        b.status = BookingStatus.CONFIRMED;
        return b;
    }

    public void cancel() {
        if (this.status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("CONFIRMED 상태의 예매만 취소할 수 있습니다.");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    private static String generateBookingNumber() {
        return "BK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%06d", (long) (Math.random() * 1_000_000));
    }
}
