package com.pio.nexverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCourseModuleRequestDTO {
    @NotBlank
    private String title;
    private String shortDescription;
    private String description;

    @NotNull
    private Boolean isMandatory;
}