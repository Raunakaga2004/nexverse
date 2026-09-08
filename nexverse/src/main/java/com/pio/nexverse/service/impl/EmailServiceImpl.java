package com.pio.nexverse.service.impl;

import com.pio.nexverse.constants.AppConstants;
import com.pio.nexverse.exception.EmailDeliveryException;
import com.pio.nexverse.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Async
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@nexverse.com}")
    private String senderEmail;

    @Value("${app.frontend.base-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String recipientEmail, String token) {
        log.info("Sending password reset email. recipientEmail = {}", recipientEmail);
        Context context = new Context();
        context.setVariable("setPasswordLink", frontendUrl + AppConstants.RESET_PASSWORD_PATH + token);
        context.setVariable("title", "Reset Your Password");
        context.setVariable("body", "We received a request to reset the password for your Nexverse account. Click the button below to set up a new password:");
        String body = templateEngine.process("email/set-password", context);
        String subject = "Reset your NexVerse Password";
        sendMail(recipientEmail, subject, body);
        log.info("Password reset email sent successfully. recipientEmail = {}", recipientEmail);
    }

    public void sendAccountActivationEmail(String recipientEmail, String token, String organizationName) {
        log.info("Sending account activation email. recipientEmail = {}, organizationName = {}", recipientEmail, organizationName);
        Context context = new Context();
        context.setVariable("setPasswordLink", frontendUrl + AppConstants.ACTIVATE_ACCOUNT_PATH + token);
        context.setVariable("title", "Activate Your Account");
        context.setVariable("body", "Your Account for " + organizationName + " has been created successfully. Click the button below to activate and set your password:");
        String body = templateEngine.process("email/set-password", context);
        String subject = "Activate your NexVerse Account";
        sendMail(recipientEmail, subject, body);
        log.info("Account activation email sent successfully. recipientEmail = {}, organizationName = {}", recipientEmail, organizationName);
    }

    private void sendMail(String recipientEmail, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addInline("nexverse-logo", new ClassPathResource("templates/email/images/nexverse-logo.png"), "image/png");
            mailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            log.error("Failed to send email recipientEmail = {}, subject = {}", recipientEmail, subject, exception);
            throw new EmailDeliveryException(recipientEmail);
        }
    }
}