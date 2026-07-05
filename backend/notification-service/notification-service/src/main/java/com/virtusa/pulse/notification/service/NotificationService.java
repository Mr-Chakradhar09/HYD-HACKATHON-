package com.virtusa.pulse.notification.service;

import com.virtusa.pulse.notification.dto.NotificationRequest;
import com.virtusa.pulse.notification.dto.NotificationResponse;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
}
