package com.pio.nexverse.dto;

import com.pio.nexverse.enums.OrganizationStatus;
import lombok.Data;

@Data
public class OrganizationSearchRequestDTO {
    private OrganizationStatus status;
    private String search;
    private Boolean isEnabled;
}