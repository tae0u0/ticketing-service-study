package com.ticketing.payment.dto;

import com.ticketing.payment.domain.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentResponse {
    private final Long paymentId;
    private final Long reservationId;
    private final Long userId;
    private final int amount;
    private final String status;
    private final String pgTransactionId;
    private final String failedReason;
    private final LocalDateTime createdAt;

    private PaymentResponse(Payment p) {
        this.paymentId = p.getId();
        this.reservationId = p.getReservationId();
        this.userId = p.getUserId();
        this.amount = p.getAmount();
        this.status = p.getStatus().name();
        this.pgTransactionId = p.getPgTransactionId();
        this.failedReason = p.getFailedReason();
        this.createdAt = p.getCreatedAt();
    }

    public static PaymentResponse of(Payment p) {
        return new PaymentResponse(p);
    }
}
