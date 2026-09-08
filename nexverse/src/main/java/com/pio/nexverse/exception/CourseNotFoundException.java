package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.COURSE_NOT_FOUND;

public class CourseNotFoundException extends RuntimeException{
    public CourseNotFoundException(){
        super(COURSE_NOT_FOUND);
    }
}