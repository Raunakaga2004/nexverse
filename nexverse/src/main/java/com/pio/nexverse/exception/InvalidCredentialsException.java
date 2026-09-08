package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super(ExceptionMessages.INVALID_CREDENTIALS);
    }
}
