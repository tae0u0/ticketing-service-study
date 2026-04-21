package com.ticketing.reservation.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedissonClient redissonClient;

    private static final String SEAT_LOCK_PREFIX = "seat:lock:";
    private static final long WAIT_TIME_SECONDS = 0;      // 즉시 실패 (다른 사람이 락 잡은 경우)
    private static final long LEASE_TIME_SECONDS = 10;    // 10초 이상 비즈니스 로직이면 연장 필요

    /**
     * 좌석 분산 락을 획득하고 supplier를 실행한다.
     * 락 획득 실패 시 LockAcquisitionException 을 던진다.
     */
    public <T> T executeWithSeatLock(Long seatId, Supplier<T> supplier) {
        String lockKey = SEAT_LOCK_PREFIX + seatId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("좌석 락 획득 실패 seatId={}", seatId);
                throw new LockAcquisitionException("좌석이 이미 처리 중입니다. seatId=" + seatId);
            }
            log.debug("좌석 락 획득 성공 seatId={}", seatId);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("락 획득 중 인터럽트 발생");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("좌석 락 해제 seatId={}", seatId);
            }
        }
    }

    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}
