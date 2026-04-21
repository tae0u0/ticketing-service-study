package com.ticketing.booking.outbox;

import com.ticketing.booking.domain.OutboxEvent;
import com.ticketing.booking.repository.OutboxEventRepository;
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

    private static final String BOOKING_TOPIC = "booking.events";

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(BOOKING_TOPIC, event.getAggregateId().toString(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Outbox 발행 실패 id={} error={}", event.getId(), ex.getMessage());
                            }
                        });
                event.markPublished();
            } catch (Exception e) {
                log.error("Outbox 처리 오류 id={}", event.getId(), e);
            }
        }
    }
}
