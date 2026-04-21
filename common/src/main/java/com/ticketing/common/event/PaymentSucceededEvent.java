package com.ticketing.common.event;

public class PaymentSucceededEvent extends DomainEvent {

    private Long paymentId;
    private Long reservationId;
    private Long userId;
    private Long seatId;
    private Long domainEventId;
    private int amount;
    private String pgTransactionId;

    public PaymentSucceededEvent() {
        super("PaymentSucceeded");
    }

    public PaymentSucceededEvent(Long paymentId, Long reservationId, Long userId,
                                  Long seatId, Long domainEventId, int amount, String pgTransactionId) {
        super("PaymentSucceeded");
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.userId = userId;
        this.seatId = seatId;
        this.domainEventId = domainEventId;
        this.amount = amount;
        this.pgTransactionId = pgTransactionId;
    }

    public Long getPaymentId() { return paymentId; }
    public Long getReservationId() { return reservationId; }
    public Long getUserId() { return userId; }
    public Long getSeatId() { return seatId; }
    public Long getDomainEventId() { return domainEventId; }
    public int getAmount() { return amount; }
    public String getPgTransactionId() { return pgTransactionId; }
}
