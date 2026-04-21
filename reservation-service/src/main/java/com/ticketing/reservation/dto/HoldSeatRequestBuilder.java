package com.ticketing.reservation.dto;

public class HoldSeatRequestBuilder {

    private final HoldSeatRequest original;
    private final String idempotencyKey;

    public HoldSeatRequestBuilder(HoldSeatRequest original, String idempotencyKey) {
        this.original = original;
        this.idempotencyKey = idempotencyKey;
    }

    public HoldSeatRequest build() {
        return new HoldSeatRequest(original.getEventId(), original.getSeatId(), idempotencyKey);
    }
}
