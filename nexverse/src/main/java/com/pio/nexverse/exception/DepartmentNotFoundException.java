package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class DepartmentNotFoundException extends RuntimeException {
    public DepartmentNotFoundException() {
        super(ExceptionMessages.DEPARTMENT_NOT_FOUND);
    }
}