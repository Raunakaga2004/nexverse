package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class DepartmentDisableException extends RuntimeException{
    public DepartmentDisableException() {
        super(ExceptionMessages.DEPARTMENT_DISABLED);
    }
}