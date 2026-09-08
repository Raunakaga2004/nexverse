package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.MODULE_NOT_FOUND;

public class CourseModuleNotFoundException extends RuntimeException {
    public CourseModuleNotFoundException() {
        super(MODULE_NOT_FOUND);
    }
}