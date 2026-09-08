package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class InvalidSetPasswordTokenException extends RuntimeException {
    public InvalidSetPasswordTokenException() {
        super(ExceptionMessages.INVALID_SET_PASS_TOKEN);
    }
}