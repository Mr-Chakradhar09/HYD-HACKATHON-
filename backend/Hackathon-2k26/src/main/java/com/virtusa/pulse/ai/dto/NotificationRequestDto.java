package com.virtusa.pulse.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class NotificationRequestDto {

    @NotBlank(message = "Recipient role cannot be blank")
    private String recipientRole;

    private String location;

    @NotBlank(message = "Notification type cannot be blank")
    private String type; // e.g. EMAIL, SLACK, PUSH

    @NotBlank(message = "Subject cannot be blank")
    private String subject;

    @NotBlank(message = "Body cannot be blank")
    private String body;

    public NotificationRequestDto() {
    }

    public NotificationRequestDto(String recipientRole, String location, String type, String subject, String body) {
        this.recipientRole = recipientRole;
        this.location = location;
        this.type = type;
        this.subject = subject;
        this.body = body;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationRequestDto that = (NotificationRequestDto) o;
        return Objects.equals(recipientRole, that.recipientRole) &&
                Objects.equals(location, that.location) &&
                Objects.equals(type, that.type) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientRole, location, type, subject, body);
    }

    @Override
    public String toString() {
        return "NotificationRequestDto{" +
                "recipientRole='" + recipientRole + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
