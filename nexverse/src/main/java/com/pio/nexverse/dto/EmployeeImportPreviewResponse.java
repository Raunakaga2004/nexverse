package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeImportPreviewResponse {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private List<EmployeeImportErrorDTO> errors;
    private List<EmployeeImportRowDTO> validEmployees;
}
