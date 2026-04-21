package com.ticketing.event.controller;

import com.ticketing.common.response.ApiResponse;
import com.ticketing.event.domain.EventCategory;
import com.ticketing.event.dto.EventResponse;
import com.ticketing.event.dto.SeatResponse;
import com.ticketing.event.service.EventService;
import com.ticketing.event.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEvents(
            @RequestParam(required = false) EventCategory category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvents(category, pageable)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(eventId)));
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok(seatService.getSeats(eventId)));
    }
}
