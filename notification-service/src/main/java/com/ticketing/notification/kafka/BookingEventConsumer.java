package com.ticketing.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketing.common.event.BookingCancelledEvent;
import com.ticketing.common.event.BookingConfirmedEvent;
import com.ticketing.common.event.DomainEvent;
import com.ticketing.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "booking.events",
            groupId = "notification-service-booking",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBookingEvent(@Payload DomainEvent event) {
        log.info("예매 이벤트 수신 type={} eventId={}", event.getEventType(), event.getEventId());
        try {
            if (event instanceof BookingConfirmedEvent e) {
                notificationService.sendBookingConfirmedNotification(e);
            } else if (event instanceof BookingCancelledEvent e) {
                notificationService.sendBookingCancelledNotification(e);
            }
        } catch (Exception ex) {
            log.error("알림 이벤트 처리 실패 eventId={} error={}", event.getEventId(), ex.getMessage());
            throw ex;
        }
    }

    @Configuration
    static class KafkaConfig {

        @Value("${spring.kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Bean
        public ConsumerFactory<String, DomainEvent> consumerFactory() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            JsonDeserializer<DomainEvent> deserializer = new JsonDeserializer<>(DomainEvent.class, objectMapper);
            deserializer.addTrustedPackages("com.ticketing.common.event");

            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory() {
            var factory = new ConcurrentKafkaListenerContainerFactory<String, DomainEvent>();
            factory.setConsumerFactory(consumerFactory());
            factory.setConcurrency(2);
            factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
            return factory;
        }
    }
}
