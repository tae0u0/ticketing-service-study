package com.ticketing.event.dto;

import com.ticketing.event.domain.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateEventRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String venue;

    @NotNull
    private EventCategory category;

    private String description;

    @NotNull
    @Future
    private LocalDateTime eventDate;

    @NotNull
    private LocalDateTime bookingOpenAt;

    @NotNull
    private LocalDateTime bookingCloseAt;
}
