package com.ticketing.reservation.exception;

import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.response.ApiResponse;
import com.ticketing.reservation.lock.DistributedLockService.LockAcquisitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = switch (e.getErrorCode()) {
            case SEAT_ALREADY_HELD, DUPLICATE_RESERVATION_REQUEST -> HttpStatus.CONFLICT;
            case RESERVATION_EXPIRED, BOOKING_NOT_OPEN -> HttpStatus.BAD_REQUEST;
            case RESERVATION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockException(LockAcquisitionException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("SEAT_002", "해당 좌석은 이미 선점 중입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_001", "서버 오류가 발생했습니다."));
    }
}
