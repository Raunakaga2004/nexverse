package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException() {
        super(ExceptionMessages.ORGANIZATION_NOT_FOUND);
    }
}