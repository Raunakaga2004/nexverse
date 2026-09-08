package com.pio.nexverse.exception;

import com.pio.nexverse.constants.ExceptionMessages;

public class InvalidExcelFileException extends RuntimeException {
    public InvalidExcelFileException() {
        super(ExceptionMessages.EXCEL_FILE_ERROR);
    }
}