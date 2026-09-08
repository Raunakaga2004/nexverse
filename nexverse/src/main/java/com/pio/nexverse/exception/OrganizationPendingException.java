package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.ORGANIZATION_PENDING;

public class OrganizationPendingException extends RuntimeException {
    public OrganizationPendingException() {
        super(ORGANIZATION_PENDING);
    }
}