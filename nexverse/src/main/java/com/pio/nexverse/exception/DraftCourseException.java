package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class DraftCourseException extends RuntimeException {
    public DraftCourseException() {
        super(ExceptionMessages.DRAFT_COURSE);
    }
}