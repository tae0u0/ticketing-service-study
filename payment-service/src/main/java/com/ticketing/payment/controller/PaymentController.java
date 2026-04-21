package com.ticketing.payment.controller;

import com.ticketing.common.response.ApiResponse;
import com.ticketing.payment.dto.PaymentRequest;
import com.ticketing.payment.dto.PaymentResponse;
import com.ticketing.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        PaymentRequest finalRequest = new PaymentRequest(
                request.getReservationId(), request.getSeatId(), request.getEventId(),
                request.getAmount(), request.getPaymentMethod(), idempotencyKey
        );
        return ResponseEntity.ok(ApiResponse.ok(paymentService.pay(userId, finalRequest)));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPayment(paymentId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getMyPayments(userId)));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(
            @PathVariable Long paymentId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.refund(paymentId, userId)));
    }
}
