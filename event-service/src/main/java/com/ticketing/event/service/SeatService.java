package com.ticketing.event.service;

import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.exception.ErrorCode;
import com.ticketing.event.domain.Seat;
import com.ticketing.event.dto.SeatResponse;
import com.ticketing.event.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long eventId) {
        return seatRepository.findByEventId(eventId).stream()
                .map(SeatResponse::new)
                .collect(Collectors.toList());
    }

    // Reservation Service 에서 내부 API로 호출
    @Transactional
    public void holdSeat(Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        if (seat.getStatus() != com.ticketing.event.domain.SeatStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
        }
        seat.hold();
    }

    @Transactional
    public void releaseSeat(Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        seat.release();
        log.info("좌석 해제 완료 seatId={}", seatId);
    }

    @Transactional
    public void bookSeat(Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        seat.book();
        log.info("좌석 예매 확정 seatId={}", seatId);
    }

    @Transactional
    public void cancelSeat(Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        seat.cancel();
        log.info("좌석 취소 복구 seatId={}", seatId);
    }
}
