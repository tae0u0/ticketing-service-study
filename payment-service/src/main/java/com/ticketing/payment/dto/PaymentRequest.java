package com.ticketing.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private Long reservationId;

    @NotNull
    private Long seatId;

    @NotNull
    private Long eventId;

    @Min(100)
    private int amount;

    @NotBlank
    private String paymentMethod;

    private String idempotencyKey;
}
