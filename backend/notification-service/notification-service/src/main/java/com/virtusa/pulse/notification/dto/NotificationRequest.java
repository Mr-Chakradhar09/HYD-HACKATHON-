package com.virtusa.pulse.notification.dto;

import com.virtusa.pulse.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationRequest {

    @NotBlank(message = "Recipient cannot be empty")
    private String recipient;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    @NotNull(message = "Channel cannot be empty")
    private NotificationChannel channel;

    public NotificationRequest() {
    }

    public NotificationRequest(String recipient, String message, NotificationChannel channel) {
        this.recipient = recipient;
        this.message = message;
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "recipient='" + recipient + '\'' +
                ", message='" + message + '\'' +
                ", channel=" + channel +
                '}';
    }
}
