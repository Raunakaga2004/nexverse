package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class CourseNotInDraftException extends RuntimeException {
    public CourseNotInDraftException() {
        super(ExceptionMessages.COURSE_MUST_BE_DRAFT);
    }
}
