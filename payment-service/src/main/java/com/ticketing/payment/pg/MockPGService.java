package com.ticketing.payment.pg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 실제 PG사 연동 대신 사용하는 Mock 서비스.
 * 실무에서는 Toss Payments, KG이니시스 등 SDK로 교체.
 */
@Slf4j
@Service
public class MockPGService {

    private static final double FAILURE_RATE = 0.05; // 5% 확률 실패

    public PGResult requestPayment(Long paymentId, int amount, String paymentMethod) {
        log.info("PG 결제 요청 paymentId={} amount={}", paymentId, amount);

        // 테스트용 실패 시뮬레이션
        if (Math.random() < FAILURE_RATE) {
            log.warn("PG 결제 실패 (시뮬레이션) paymentId={}", paymentId);
            return PGResult.failure("INSUFFICIENT_BALANCE");
        }

        String pgTransactionId = "PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("PG 결제 성공 paymentId={} pgTxId={}", paymentId, pgTransactionId);
        return PGResult.success(pgTransactionId);
    }

    public PGResult requestRefund(String pgTransactionId, int amount) {
        log.info("PG 환불 요청 pgTxId={} amount={}", pgTransactionId, amount);
        return PGResult.success("REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public record PGResult(boolean success, String pgTransactionId, String failureReason) {
        public static PGResult success(String pgTransactionId) {
            return new PGResult(true, pgTransactionId, null);
        }
        public static PGResult failure(String reason) {
            return new PGResult(false, null, reason);
        }
    }
}
