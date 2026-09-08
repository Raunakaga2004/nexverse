package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;
import org.springframework.security.core.AuthenticationException;

public class UserSuspendedException extends AuthenticationException {
    public UserSuspendedException() {
        super(ExceptionMessages.USER_ACCOUNT_SUSPENDED);
    }
}