package com.ticketing.event.repository;

import com.ticketing.event.domain.Event;
import com.ticketing.event.domain.EventCategory;
import com.ticketing.event.domain.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    Page<Event> findByStatusAndCategory(EventStatus status, EventCategory category, Pageable pageable);
}
