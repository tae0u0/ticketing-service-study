package com.ticketing.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    void prePersist() {
        this.processedAt = LocalDateTime.now();
    }

    public static ProcessedEvent of(String eventId) {
        ProcessedEvent e = new ProcessedEvent();
        e.eventId = eventId;
        return e;
    }
}
