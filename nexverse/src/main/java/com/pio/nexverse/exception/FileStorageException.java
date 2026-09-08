package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class FileStorageException extends RuntimeException {
    public FileStorageException() {
        super(ExceptionMessages.FILE_STORAGE_ERROR);
    }

    public FileStorageException(String message) {
        super(message);
    }
}