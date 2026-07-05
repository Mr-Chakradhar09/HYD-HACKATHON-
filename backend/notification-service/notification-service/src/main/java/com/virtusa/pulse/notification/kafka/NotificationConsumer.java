package com.virtusa.pulse.notification.kafka;

import com.virtusa.pulse.notification.dto.NotificationRequest;
import com.virtusa.pulse.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeNotification(NotificationRequest request) {
        logger.info("Received message from notification-topic: {}", request);
        try {
            notificationService.sendNotification(request);
            logger.info("Kafka notification processed successfully");
        } catch (Exception e) {
            logger.error("Error processing notification received via Kafka: {}", e.getMessage(), e);
        }
    }
}
