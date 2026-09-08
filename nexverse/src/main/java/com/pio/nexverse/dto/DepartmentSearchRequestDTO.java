package com.pio.nexverse.dto;

import lombok.Data;

@Data
public class DepartmentSearchRequestDTO {
    private String search;
    private Boolean isEnabled;
}