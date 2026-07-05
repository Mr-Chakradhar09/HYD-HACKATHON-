package com.pulse.survey.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL:http://localhost:8084}")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class NotificationRequest {
        private String recipientRole; // e.g. EMPLOYEE, HR, GLOBAL_HR
        private String location;       // target location
        private String title;
        private String message;
        private String type;           // e.g. SURVEY_PUBLISHED, SURVEY_CLOSED, DRAFT_GENERATED
    }
}
