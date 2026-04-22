package com.ticketing.payment.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter paymentSuccessCounter(MeterRegistry registry) {
        return Counter.builder("ticketing.payment.success")
                .description("결제 성공 건수")
                .register(registry);
    }

    @Bean
    public Counter paymentFailureCounter(MeterRegistry registry) {
        return Counter.builder("ticketing.payment.failure")
                .description("결제 실패 건수")
                .register(registry);
    }
}
