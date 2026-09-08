package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;
import org.springframework.security.core.AuthenticationException;

public class OrganizationDisabledException extends AuthenticationException {
    public OrganizationDisabledException() {
        super(ExceptionMessages.ORGANIZATION_DISABLED);
    }
}