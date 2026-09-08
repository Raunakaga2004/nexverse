package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeImportErrorDTO {
    private int rowNumber;
    private int columnIndex;
    private String columnName;
    private String rejectedValue;
    private String reason;
}