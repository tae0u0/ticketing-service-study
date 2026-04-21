package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdempotencyKey(String idempotencyKey);

    Optional<Reservation> findBySeatIdAndStatus(Long seatId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'HELD' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
