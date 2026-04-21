package com.ticketing.reservation.scheduler;

import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    /**
     * 30초마다 만료된 선점 예약을 처리한다.
     * Redis Keyspace Notification의 보조 수단 (이중화).
     */
    @Scheduled(fixedDelay = 30_000)
    public void expireHeldReservations() {
        List<Long> expiredIds = reservationRepository
                .findExpiredReservations(LocalDateTime.now())
                .stream()
                .map(r -> r.getId())
                .toList();

        if (expiredIds.isEmpty()) return;

        log.info("만료 예약 처리 시작 count={}", expiredIds.size());
        int successCount = 0;
        for (Long id : expiredIds) {
            try {
                reservationService.expireReservation(id);
                successCount++;
            } catch (Exception e) {
                log.error("만료 처리 실패 reservationId={} error={}", id, e.getMessage());
            }
        }
        log.info("만료 예약 처리 완료 success={} total={}", successCount, expiredIds.size());
    }
}
