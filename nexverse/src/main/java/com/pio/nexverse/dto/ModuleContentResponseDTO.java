package com.pio.nexverse.dto;

import com.pio.nexverse.enums.ContentType;
import lombok.Data;

@Data
public class ModuleContentResponseDTO {
    private Long id;
    private String title;
    private String description;
    private ContentType contentType;
    private String textBody;
    private Integer sequenceOrder;
    private Boolean isMandatory;
    private Integer estimatedDurationSeconds;
}