package com.pio.nexverse.exception;

import static com.pio.nexverse.constants.ExceptionMessages.CONTENT_NOT_FOUND;

public class ContentNotFoundException extends RuntimeException{
    public ContentNotFoundException(){
        super(CONTENT_NOT_FOUND);
    }
}