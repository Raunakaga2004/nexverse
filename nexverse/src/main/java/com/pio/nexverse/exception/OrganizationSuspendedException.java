package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;
import org.springframework.security.core.AuthenticationException;

public class OrganizationSuspendedException extends AuthenticationException {
    public OrganizationSuspendedException() {
        super(ExceptionMessages.ORGANIZATION_SUSPENDED);
    }
}