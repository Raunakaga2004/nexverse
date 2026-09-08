package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.COURSE_REQUEST_NOT_FOUND;

public class CourseRequestNotFoundException extends RuntimeException {
    public CourseRequestNotFoundException() {
        super(COURSE_REQUEST_NOT_FOUND);
    }
}
