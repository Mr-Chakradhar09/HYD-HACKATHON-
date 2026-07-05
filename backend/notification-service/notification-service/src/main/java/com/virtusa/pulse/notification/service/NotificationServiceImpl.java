package com.virtusa.pulse.notification.service;

import com.virtusa.pulse.notification.dto.NotificationRequest;
import com.virtusa.pulse.notification.dto.NotificationResponse;
import com.virtusa.pulse.notification.entity.Notification;
import com.virtusa.pulse.notification.entity.NotificationStatus;
import com.virtusa.pulse.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final String fromEmail;

    public NotificationServiceImpl(NotificationRepository notificationRepository, 
                                   JavaMailSender mailSender, 
                                   CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                                   @Value("${spring.mail.username:noreply@virtusa.com}") String fromEmail) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.fromEmail = fromEmail;
    }

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        logger.info("Processing notification dispatch to recipient: {} via channel: {}", 
                request.getRecipient(), request.getChannel());

        // Perform dispatch (wrapped in circuit breaker)
        NotificationStatus status = dispatch(request);

        // Map and save to database
        Notification notification = new Notification();
        notification.setRecipient(request.getRecipient());
        notification.setMessage(request.getMessage());
        notification.setChannel(request.getChannel());
        notification.setStatus(status);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        logger.info("Notification log saved successfully with ID: {} and status: {}", saved.getId(), saved.getStatus());

        return mapToResponse(saved);
    }

    private NotificationStatus dispatch(NotificationRequest request) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("notificationDispatch");
        return circuitBreaker.run(() -> {
            try {
                switch (request.getChannel()) {
                    case EMAIL:
                        logger.info("Sending Email to {}", request.getRecipient());
                        SimpleMailMessage mailMessage = new SimpleMailMessage();
                        mailMessage.setFrom(fromEmail);
                        mailMessage.setTo(request.getRecipient());
                        mailMessage.setSubject("Pulse Survey Notification");
                        mailMessage.setText(request.getMessage());
                        mailSender.send(mailMessage);
                        logger.info("Email sent successfully to {}", request.getRecipient());
                        break;
                    case SLACK:
                        logger.info("Mock Slack API: Posting to Slack channel/user {}", request.getRecipient());
                        break;
                    case IN_APP:
                        logger.info("Mock In-App: Rendering notification feed for {}", request.getRecipient());
                        break;
                    default:
                        logger.warn("Unknown notification channel: {}", request.getChannel());
                        return NotificationStatus.FAILED;
                }
                return NotificationStatus.SENT;
            } catch (Exception e) {
                logger.error("Failed to dispatch notification to {}: {}", request.getRecipient(), e.getMessage(), e);
                throw new RuntimeException("Dispatch failed", e);
            }
        }, throwable -> {
            logger.error("Circuit breaker fallback triggered for {}. Exception: ", request.getRecipient(), throwable);
            return NotificationStatus.FAILED;
        });
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient(),
                notification.getMessage(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
