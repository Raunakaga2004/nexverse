package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.USER_NOT_SUSPENDED;

public class UserNotSuspendedException extends RuntimeException {
    public UserNotSuspendedException() {
        super(USER_NOT_SUSPENDED);
    }
}
