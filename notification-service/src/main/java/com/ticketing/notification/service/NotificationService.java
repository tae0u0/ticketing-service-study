package com.ticketing.notification.service;

import com.ticketing.common.event.BookingCancelledEvent;
import com.ticketing.common.event.BookingConfirmedEvent;
import com.ticketing.notification.domain.NotificationLog;
import com.ticketing.notification.domain.ProcessedEvent;
import com.ticketing.notification.repository.NotificationLogRepository;
import com.ticketing.notification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationLogRepository notificationLogRepository;

    @Transactional
    public void sendBookingConfirmedNotification(BookingConfirmedEvent event) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("중복 알림 이벤트 skip eventId={}", event.getEventId());
            return;
        }

        String title = "예매가 확정되었습니다.";
        String content = String.format("예매번호: %s | 금액: %,d원",
                event.getBookingNumber(), event.getAmount());

        // 실제 이메일/SMS 발송 대신 로그로 대체
        log.info("[알림 발송] userId={} title={} content={}", event.getUserId(), title, content);

        notificationLogRepository.save(
                NotificationLog.create(event.getUserId(), "BOOKING_CONFIRMED", title, content)
        );
        processedEventRepository.save(ProcessedEvent.of(event.getEventId()));
    }

    @Transactional
    public void sendBookingCancelledNotification(BookingCancelledEvent event) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            return;
        }

        String title = "예매가 취소되었습니다.";
        String content = String.format("예매 취소 처리가 완료되었습니다. 환불금액: %,d원", event.getRefundAmount());

        log.info("[알림 발송] userId={} title={} content={}", event.getUserId(), title, content);

        notificationLogRepository.save(
                NotificationLog.create(event.getUserId(), "BOOKING_CANCELLED", title, content)
        );
        processedEventRepository.save(ProcessedEvent.of(event.getEventId()));
    }
}
