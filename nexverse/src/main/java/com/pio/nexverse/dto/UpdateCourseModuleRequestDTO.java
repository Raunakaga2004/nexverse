package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCourseModuleRequestDTO {
    private String title;
    private String shortDescription;
    private String description;
    private Boolean isMandatory;
}