package com.virtusa.pulse.ai.producer;

import com.virtusa.pulse.ai.dto.NotificationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final KafkaTemplate<String, NotificationRequestDto> kafkaTemplate;

    @Value("${app.kafka.notification-topic:notification-topic}")
    private String topic;

    public NotificationProducer(KafkaTemplate<String, NotificationRequestDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationRequestDto notificationRequest) {
        log.info("Publishing notification event to Kafka topic '{}': {}", topic, notificationRequest);
        try {
            kafkaTemplate.send(topic, notificationRequest.getRecipientRole(), notificationRequest);
            log.info("Successfully published notification event to Kafka");
        } catch (Exception e) {
            log.error("Failed to publish notification event to Kafka: {}", e.getMessage(), e);
        }
    }
}
