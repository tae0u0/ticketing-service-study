package com.ticketing.booking.repository;

import com.ticketing.booking.domain.Booking;
import com.ticketing.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByReservationId(Long reservationId);
    Optional<Booking> findByPaymentId(Long paymentId);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookingStatus status);
}
