package com.virtusa.pulse.notification.config;

import com.virtusa.pulse.notification.entity.Notification;
import com.virtusa.pulse.notification.entity.NotificationChannel;
import com.virtusa.pulse.notification.entity.NotificationStatus;
import com.virtusa.pulse.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final NotificationRepository notificationRepository;

    public DataInitializer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (notificationRepository.count() == 0) {
            logger.info("Initializing database with dummy notification logs...");

            notificationRepository.save(new Notification(
                    "employee1@virtusa.com",
                    "Welcome to Pulse Survey! Your baseline survey is now active.",
                    NotificationChannel.EMAIL,
                    NotificationStatus.SENT,
                    LocalDateTime.now().minusHours(2)
            ));

            notificationRepository.save(new Notification(
                    "#pulse-alerts",
                    "Alert: Baseline survey response rate for location 'Dallas' has reached 80%.",
                    NotificationChannel.SLACK,
                    NotificationStatus.SENT,
                    LocalDateTime.now().minusHours(1)
            ));

            notificationRepository.save(new Notification(
                    "user123",
                    "You have a pending survey matching your location context.",
                    NotificationChannel.IN_APP,
                    NotificationStatus.PENDING,
                    LocalDateTime.now()
            ));

            logger.info("Database initialized successfully with 3 dummy notification logs.");
        } else {
            logger.info("Database already contains notification logs. Skipping dummy data initialization.");
        }
    }
}
