package com.ticketing.event.dto;

import com.ticketing.event.domain.Event;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class EventResponse {
    private final Long id;
    private final String title;
    private final String venue;
    private final String category;
    private final String description;
    private final LocalDateTime eventDate;
    private final LocalDateTime bookingOpenAt;
    private final LocalDateTime bookingCloseAt;
    private final String status;
    private List<GradeInfo> grades;

    public EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.venue = event.getVenue();
        this.category = event.getCategory().name();
        this.description = event.getDescription();
        this.eventDate = event.getEventDate();
        this.bookingOpenAt = event.getBookingOpenAt();
        this.bookingCloseAt = event.getBookingCloseAt();
        this.status = event.getStatus().name();
    }

    public void setGrades(List<GradeInfo> grades) {
        this.grades = grades;
    }

    @Getter
    public static class GradeInfo {
        private final String grade;
        private final int price;
        private final long totalCount;
        private final long availableCount;

        public GradeInfo(String grade, int price, long totalCount, long availableCount) {
            this.grade = grade;
            this.price = price;
            this.totalCount = totalCount;
            this.availableCount = availableCount;
        }
    }
}
