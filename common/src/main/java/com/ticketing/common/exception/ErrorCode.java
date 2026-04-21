package com.ticketing.common.exception;

public enum ErrorCode {

    // 좌석
    SEAT_NOT_FOUND("SEAT_001", "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_HELD("SEAT_002", "해당 좌석은 이미 선점 중입니다."),
    SEAT_NOT_AVAILABLE("SEAT_003", "예매 가능한 좌석이 아닙니다."),

    // 예약
    RESERVATION_NOT_FOUND("RESERVATION_001", "예약을 찾을 수 없습니다."),
    RESERVATION_EXPIRED("RESERVATION_002", "좌석 선점이 만료되었습니다. 다시 선택해주세요."),
    RESERVATION_ALREADY_EXISTS("RESERVATION_003", "이미 진행 중인 예약이 있습니다."),
    DUPLICATE_RESERVATION_REQUEST("RESERVATION_004", "동일한 예약 요청이 처리 중입니다."),

    // 결제
    PAYMENT_NOT_FOUND("PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    DUPLICATE_PAYMENT("PAYMENT_002", "이미 처리된 결제 요청입니다."),
    PAYMENT_FAILED("PAYMENT_003", "결제에 실패하였습니다."),
    PAYMENT_AMOUNT_MISMATCH("PAYMENT_004", "결제 금액이 일치하지 않습니다."),

    // 예매
    BOOKING_NOT_FOUND("BOOKING_001", "예매 내역을 찾을 수 없습니다."),
    BOOKING_CANCEL_NOT_ALLOWED("BOOKING_002", "취소 불가한 예매입니다."),
    BOOKING_ALREADY_CANCELLED("BOOKING_003", "이미 취소된 예매입니다."),

    // 공연
    EVENT_NOT_FOUND("EVENT_001", "공연을 찾을 수 없습니다."),
    BOOKING_NOT_OPEN("EVENT_002", "예매 가능 시간이 아닙니다."),

    // 사용자
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("USER_002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD("USER_003", "비밀번호가 올바르지 않습니다."),

    // 공통
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다."),
    FORBIDDEN("AUTH_002", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("SERVER_001", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
