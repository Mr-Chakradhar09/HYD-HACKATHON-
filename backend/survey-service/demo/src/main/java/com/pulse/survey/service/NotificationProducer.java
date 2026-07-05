package com.pulse.survey.service;

import com.pulse.survey.client.NotificationClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationClient notificationClient;

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate, NotificationClient notificationClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationClient = notificationClient;
    }

    public void sendSurveyPublishedNotification(Long surveyId, String title, String location) {
        String message = String.format("A new employee pulse survey '%s' has been published for your location (%s). Please submit your responses.", title, location);
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type("SURVEY_PUBLISHED")
                .surveyId(surveyId)
                .title("New Survey Published")
                .message(message)
                .location(location)
                .recipientRole("EMPLOYEE")
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(event);
    }

    public void sendSurveyClosedNotification(Long surveyId, String title, String location) {
        String message = String.format("The pulse survey '%s' for location (%s) has been closed. AI question generation is now available.", title, location);
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type("SURVEY_CLOSED")
                .surveyId(surveyId)
                .title("Survey Closed")
                .message(message)
                .location(location)
                .recipientRole("HR")
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(event);
    }

    public void sendDraftQuestionsGeneratedNotification(Long surveyId, int draftCount) {
        String message = String.format("AI has successfully analyzed survey responses for Survey ID %d and generated %d draft questions for review.", surveyId, draftCount);
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type("DRAFT_GENERATED")
                .surveyId(surveyId)
                .title("AI Draft Questions Generated")
                .message(message)
                .location("GLOBAL")
                .recipientRole("GLOBAL_HR")
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(event);
    }

    private void publishEvent(NotificationEvent event) {
        try {
            log.info("Publishing notification event via Kafka: {}", event);
            kafkaTemplate.send("survey-notifications", event.getEventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish notification event to Kafka. Falling back to direct Notification Feign Client. Error: {}", e.getMessage());
            try {
                NotificationClient.NotificationRequest httpReq = NotificationClient.NotificationRequest.builder()
                        .recipientRole(event.getRecipientRole())
                        .location(event.getLocation())
                        .title(event.getTitle())
                        .message(event.getMessage())
                        .type(event.getType())
                        .build();
                notificationClient.sendNotification(httpReq);
                log.info("Successfully sent fallback notification over Feign Client.");
            } catch (Exception httpEx) {
                log.error("Failed to send fallback HTTP notification: {}", httpEx.getMessage());
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationEvent {
        private String eventId;
        private String type;
        private Long surveyId;
        private String title;
        private String message;
        private String location;
        private String recipientRole;
        private LocalDateTime timestamp;
    }
}
