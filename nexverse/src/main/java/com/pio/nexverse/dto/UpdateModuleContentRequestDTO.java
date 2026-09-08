package com.pio.nexverse.dto;

import com.pio.nexverse.enums.ContentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateModuleContentRequestDTO {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private ContentType contentType;

    @NotNull
    private Boolean isMandatory;

    @NotNull
    @Min(value = 1)
    private Integer estimatedDurationSeconds;

    private String textBody;
}