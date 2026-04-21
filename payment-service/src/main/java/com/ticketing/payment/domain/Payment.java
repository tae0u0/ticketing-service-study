package com.ticketing.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String paymentMethod;

    @Column(unique = true)
    private String pgTransactionId;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String failedReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Payment create(Long reservationId, Long userId, Long seatId, Long eventId,
                                  int amount, String paymentMethod, String idempotencyKey) {
        Payment p = new Payment();
        p.reservationId = reservationId;
        p.userId = userId;
        p.seatId = seatId;
        p.eventId = eventId;
        p.amount = amount;
        p.paymentMethod = paymentMethod;
        p.idempotencyKey = idempotencyKey;
        p.status = PaymentStatus.PENDING;
        return p;
    }

    public void succeed(String pgTransactionId) {
        this.status = PaymentStatus.SUCCESS;
        this.pgTransactionId = pgTransactionId;
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failedReason = reason;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}
