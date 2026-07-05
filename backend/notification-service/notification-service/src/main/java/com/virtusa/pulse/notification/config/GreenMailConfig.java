package com.virtusa.pulse.notification.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GreenMailConfig {

    private static final Logger logger = LoggerFactory.getLogger(GreenMailConfig.class);
    private GreenMail greenMail;

    @PostConstruct
    public void startMailServer() {
        logger.info("Starting in-memory GreenMail SMTP server on port 3025 (no authentication required)...");
        // ServerSetupTest.SMTP uses port 3025 by default
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        
        // Start the server
        greenMail.start();
        logger.info("GreenMail SMTP server started successfully.");
    }

    @PreDestroy
    public void stopMailServer() {
        if (greenMail != null) {
            logger.info("Stopping GreenMail SMTP server...");
            greenMail.stop();
            logger.info("GreenMail SMTP server stopped.");
        }
    }

    public GreenMail getGreenMail() {
        return greenMail;
    }
}
