package com.example.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "app.kafka.events.enabled", havingValue = "true")
public class KafkaConfig {

    @Bean
    public NewTopic messageEventsTopic() {
        return TopicBuilder.name("chat.message.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("chat.user.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic conversationEventsTopic() {
        return TopicBuilder.name("chat.conversation.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("chat.notification.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}