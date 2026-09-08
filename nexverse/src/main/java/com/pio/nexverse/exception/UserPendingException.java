package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.USER_PENDING;

public class UserPendingException extends RuntimeException {
    public UserPendingException() {
        super(USER_PENDING);
    }
}
