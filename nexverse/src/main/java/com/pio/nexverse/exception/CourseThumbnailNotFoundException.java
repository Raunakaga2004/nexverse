package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.COURSE_THUMBNAIL_NOT_FOUND;

public class CourseThumbnailNotFoundException extends RuntimeException {
    public CourseThumbnailNotFoundException() {
        super(COURSE_THUMBNAIL_NOT_FOUND);
    }
}
