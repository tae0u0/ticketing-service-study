package com.ticketing.event.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private LocalDateTime bookingOpenAt;

    @Column(nullable = false)
    private LocalDateTime bookingCloseAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = EventStatus.SCHEDULED;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Event create(String title, String venue, EventCategory category, String description,
                                LocalDateTime eventDate, LocalDateTime bookingOpenAt, LocalDateTime bookingCloseAt) {
        Event event = new Event();
        event.title = title;
        event.venue = venue;
        event.category = category;
        event.description = description;
        event.eventDate = eventDate;
        event.bookingOpenAt = bookingOpenAt;
        event.bookingCloseAt = bookingCloseAt;
        event.status = EventStatus.SCHEDULED;
        return event;
    }

    public boolean isBookingOpen() {
        LocalDateTime now = LocalDateTime.now();
        return status == EventStatus.OPEN
                && now.isAfter(bookingOpenAt)
                && now.isBefore(bookingCloseAt);
    }

    public void open() { this.status = EventStatus.OPEN; }
    public void close() { this.status = EventStatus.CLOSED; }
    public void cancel() { this.status = EventStatus.CANCELLED; }
}
