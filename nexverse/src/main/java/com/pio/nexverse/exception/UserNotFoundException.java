package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(ExceptionMessages.USER_NOT_FOUND);
    }
}
