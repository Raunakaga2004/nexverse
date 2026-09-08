package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String recipientEmail) {
        super(ExceptionMessages.EMAIL_SENT_ERROR + recipientEmail);
    }
}