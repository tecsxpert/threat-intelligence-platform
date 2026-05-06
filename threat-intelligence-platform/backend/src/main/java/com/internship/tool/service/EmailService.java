package com.internship.tool.service;

import com.internship.tool.entity.ThreatIntelligence;
import com.internship.tool.repository.ThreatIntelligenceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ThreatIntelligenceRepository repository;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, ThreatIntelligenceRepository repository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.repository = repository;
    }

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyReminder() {
        log.info("Running daily threat reminder scheduled task...");
        List<ThreatIntelligence> criticalThreats = repository.findBySeverityAndStatusAndDeletedFalse("CRITICAL", "OPEN");
        
        if (!criticalThreats.isEmpty()) {
            Context context = new Context();
            context.setVariable("threats", criticalThreats);
            String htmlContent = templateEngine.process("daily-reminder", context);
            
            if (htmlContent != null) {
                try {
                    sendHtmlEmail("admin@threatplatform.com", "Daily Critical Threats Reminder", htmlContent);
                } catch (MessagingException e) {
                    log.error("Failed to send daily reminder email", e);
                }
            }
        }
    }

    private void sendHtmlEmail(@org.springframework.lang.NonNull String to, 
                               @org.springframework.lang.NonNull String subject, 
                               @org.springframework.lang.NonNull String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
        log.info("Sent email to {}", to);
    }
}
