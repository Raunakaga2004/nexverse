package com.pio.nexverse.service;

public interface EmailService {
    void sendPasswordResetEmail(String recipientEmail, String resetLink);

    void sendAccountActivationEmail(String recipientEmail, String token, String organizationName);
}