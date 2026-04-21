package com.ticketing.event.controller;

import com.ticketing.common.response.ApiResponse;
import com.ticketing.event.dto.CreateEventRequest;
import com.ticketing.event.dto.CreateSeatsRequest;
import com.ticketing.event.dto.EventResponse;
import com.ticketing.event.dto.SeatResponse;
import com.ticketing.event.service.EventService;
import com.ticketing.event.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;
    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(eventService.createEvent(request)));
    }

    @PostMapping("/{eventId}/seats/bulk")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createSeats(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateSeatsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(eventService.createSeats(eventId, request)));
    }
}
