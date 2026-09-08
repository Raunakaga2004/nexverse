package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;
import org.springframework.security.core.AuthenticationException;

public class UserAccountDisabledException extends AuthenticationException {
    public UserAccountDisabledException() {
        super(ExceptionMessages.USER_ACCOUNT_DISABLED);
    }
}