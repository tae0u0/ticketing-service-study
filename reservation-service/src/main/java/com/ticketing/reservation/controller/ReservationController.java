package com.ticketing.reservation.controller;

import com.ticketing.common.response.ApiResponse;
import com.ticketing.reservation.dto.HoldSeatRequest;
import com.ticketing.reservation.dto.HoldSeatRequestBuilder;
import com.ticketing.reservation.dto.ReservationResponse;
import com.ticketing.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 좌석 임시 선점
     * Header: X-User-Id (API Gateway에서 주입)
     * Header: Idempotency-Key (클라이언트가 UUID 생성)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> holdSeat(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody HoldSeatRequest request) {

        HoldSeatRequest finalRequest = new HoldSeatRequestBuilder(request, idempotencyKey).build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(reservationService.holdSeat(userId, finalRequest)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getMyReservations(userId)));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @PathVariable Long reservationId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getReservation(reservationId, userId)));
    }

    // 내부 API: Payment Service에서 예약 확정 시 호출
    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReservation(@PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
