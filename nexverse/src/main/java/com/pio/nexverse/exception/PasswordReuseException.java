package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class PasswordReuseException extends RuntimeException {
    public PasswordReuseException() {
        super(ExceptionMessages.PASSWORD_REUSE);
    }
}