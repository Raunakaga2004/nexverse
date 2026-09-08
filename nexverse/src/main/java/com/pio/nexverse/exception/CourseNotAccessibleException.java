package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.COURSE_NOT_ACCESSIBLE;

public class CourseNotAccessibleException extends RuntimeException {
    public CourseNotAccessibleException() {
        super(COURSE_NOT_ACCESSIBLE);
    }
}