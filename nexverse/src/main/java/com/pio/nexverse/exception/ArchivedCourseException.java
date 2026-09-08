package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.ARCHIVED_COURSE;

public class ArchivedCourseException extends RuntimeException {
    public ArchivedCourseException() {
        super(ARCHIVED_COURSE);
    }
}