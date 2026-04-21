package com.ticketing.event.repository;

import com.ticketing.event.domain.Seat;
import com.ticketing.event.domain.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long eventId);

    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.eventId = :eventId AND s.status = 'AVAILABLE'")
    int countAvailableByEventId(@Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);
}
