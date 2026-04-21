package com.ticketing.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatRequest {

    @NotNull
    private Long eventId;

    @NotNull
    private Long seatId;

    private String idempotencyKey;
}
