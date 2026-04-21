package com.ticketing.event.controller;

import com.ticketing.common.response.ApiResponse;
import com.ticketing.event.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/seats")
@RequiredArgsConstructor
public class InternalSeatController {

    private final SeatService seatService;

    @PostMapping("/{seatId}/hold")
    public ResponseEntity<ApiResponse<Void>> holdSeat(@PathVariable Long seatId) {
        seatService.holdSeat(seatId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{seatId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeat(@PathVariable Long seatId) {
        seatService.releaseSeat(seatId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{seatId}/book")
    public ResponseEntity<ApiResponse<Void>> bookSeat(@PathVariable Long seatId) {
        seatService.bookSeat(seatId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{seatId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSeat(@PathVariable Long seatId) {
        seatService.cancelSeat(seatId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
