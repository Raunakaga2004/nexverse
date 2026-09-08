package com.pio.nexverse.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RowValidationResult {
    private List<EmployeeImportErrorDTO> errors;

    public RowValidationResult() {
        errors = new ArrayList<>();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addError(EmployeeImportErrorDTO error) {
        errors.add(error);
    }
}