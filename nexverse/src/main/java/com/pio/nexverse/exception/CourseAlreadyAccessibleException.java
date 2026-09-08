package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.COURSE_ACCESSIBLE;

public class CourseAlreadyAccessibleException extends RuntimeException {
    public CourseAlreadyAccessibleException() {
        super(COURSE_ACCESSIBLE);
    }
}