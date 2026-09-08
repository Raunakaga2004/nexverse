package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportEmployeeResponseDTO {
    private int totalRows;
    private int successfulImports;
    private int failedImports;
    private List<EmployeeImportErrorDTO> errors;
}
