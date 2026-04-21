package com.ticketing.reservation.outbox;

import com.ticketing.reservation.domain.OutboxEvent;
import com.ticketing.reservation.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String RESERVATION_TOPIC = "reservation.events";

    /**
     * 1초마다 미발행 Outbox 이벤트를 Kafka에 발행한다.
     * published=false인 이벤트를 최대 100건씩 처리한다.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(RESERVATION_TOPIC, event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Outbox 이벤트 발행 실패 id={} error={}", event.getId(), ex.getMessage());
                            }
                        });
                event.markPublished();
                log.debug("Outbox 이벤트 발행 id={} type={}", event.getId(), event.getEventType());
            } catch (Exception e) {
                log.error("Outbox 이벤트 처리 중 오류 id={}", event.getId(), e);
            }
        }
    }
}
