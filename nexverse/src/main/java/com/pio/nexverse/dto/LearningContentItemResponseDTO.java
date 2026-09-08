package com.pio.nexverse.dto;

import com.pio.nexverse.enums.ContentType;
import lombok.Data;

@Data
public class LearningContentItemResponseDTO {
    private Long id;
    private String title;
    private String description;
    private ContentType contentType;
    private Integer sequenceOrder;
    private Integer estimatedDurationSeconds;
    private boolean mandatory;
    private boolean started;
    private boolean completed;
}