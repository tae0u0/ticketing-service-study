package com.ticketing.payment.scheduler;

import com.ticketing.payment.domain.PaymentStatus;
import com.ticketing.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingPaymentScheduler {

    private final PaymentRepository paymentRepository;

    /**
     * 5분 이상 PENDING 상태인 결제를 모니터링.
     * 실제 운영에서는 PG사 조회 API로 최종 상태 확인 후 처리.
     */
    @Scheduled(fixedDelay = 60_000)
    public void monitorPendingPayments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        var stuckPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, threshold);

        if (!stuckPayments.isEmpty()) {
            log.warn("장기 PENDING 결제 감지 count={} - 수동 확인 필요", stuckPayments.size());
            stuckPayments.forEach(p ->
                    log.warn("장기 PENDING paymentId={} reservationId={} createdAt={}",
                            p.getId(), p.getReservationId(), p.getCreatedAt())
            );
        }
    }
}
