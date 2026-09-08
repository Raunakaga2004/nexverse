package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super(ExceptionMessages.INCORRECT_PASSWORD);
    }
}