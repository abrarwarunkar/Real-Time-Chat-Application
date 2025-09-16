package com.example.chat.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MonitoringConfig {

    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalMessages = new AtomicInteger(0);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public Counter messagesSentCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.messages.sent")
                .description("Total number of messages sent")
                .register(meterRegistry);
    }

    @Bean
    public Counter messagesDeliveredCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.messages.delivered")
                .description("Total number of messages delivered")
                .register(meterRegistry);
    }

    @Bean
    public Counter messagesReadCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.messages.read")
                .description("Total number of messages read")
                .register(meterRegistry);
    }

    @Bean
    public Timer messageDeliveryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("chat.message.delivery.time")
                .description("Time taken to deliver messages")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeConnectionsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("chat.connections.active", activeConnections, AtomicInteger::get)
                .description("Number of active WebSocket connections")
                .register(meterRegistry);
    }

    @Bean
    public Gauge onlineUsersGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("chat.users.online", this, MonitoringConfig::getOnlineUsersCount)
                .description("Number of online users")
                .register(meterRegistry);
    }

    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    private double getOnlineUsersCount() {
        try {
            Long count = redisTemplate.opsForSet().size("online_users");
            return count != null ? count.doubleValue() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}