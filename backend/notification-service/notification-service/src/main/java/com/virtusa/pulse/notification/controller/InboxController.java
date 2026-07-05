package com.virtusa.pulse.notification.controller;

import com.virtusa.pulse.notification.config.GreenMailConfig;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class InboxController {

    private final GreenMailConfig greenMailConfig;

    public InboxController(GreenMailConfig greenMailConfig) {
        this.greenMailConfig = greenMailConfig;
    }

    @GetMapping("/inbox")
    public List<Map<String, Object>> getInbox() {
        List<Map<String, Object>> emails = new ArrayList<>();
        try {
            MimeMessage[] messages = greenMailConfig.getGreenMail().getReceivedMessages();
            for (MimeMessage msg : messages) {
                Map<String, Object> email = new HashMap<>();
                email.put("subject", msg.getSubject());
                email.put("from", msg.getFrom() != null && msg.getFrom().length > 0 ? msg.getFrom()[0].toString() : "Unknown");
                
                var recipients = msg.getRecipients(Message.RecipientType.TO);
                email.put("to", recipients != null && recipients.length > 0 ? recipients[0].toString() : "Unknown");
                
                Object content = msg.getContent();
                email.put("body", content != null ? content.toString().trim() : "");
                
                emails.add(email);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve messages: " + e.getMessage());
            emails.add(error);
        }
        return emails;
    }
}
