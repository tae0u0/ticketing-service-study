package com.ticketing.common.event;

import lombok.Getter;

@Getter
public class PaymentFailedEvent extends DomainEvent {

    private Long paymentId;
    private Long reservationId;
    private Long userId;
    private Long seatId;
    private Long domainEventId;
    private String failureReason;

    public PaymentFailedEvent() {
        super("PaymentFailed");
    }

    public PaymentFailedEvent(Long paymentId, Long reservationId, Long userId,
                               Long seatId, Long domainEventId, String failureReason) {
        super("PaymentFailed");
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.domainEventId = domainEventId;
        this.failureReason = failureReason;
    }

}
