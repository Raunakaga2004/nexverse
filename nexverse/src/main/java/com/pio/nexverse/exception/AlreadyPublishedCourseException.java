package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.ALREADY_PUBLISHED_COURSE;

public class AlreadyPublishedCourseException extends RuntimeException {
    public AlreadyPublishedCourseException() {
        super(ALREADY_PUBLISHED_COURSE);
    }
}
