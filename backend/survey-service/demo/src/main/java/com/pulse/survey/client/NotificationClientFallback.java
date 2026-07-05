package com.pulse.survey.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public ResponseEntity<Void> sendNotification(NotificationRequest request) {
        log.error("NotificationClient fallback triggered for sendNotification. Notification HTTP Service is down.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
