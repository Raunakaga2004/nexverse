package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CompletionStatus;
import com.pio.nexverse.enums.ContentType;
import lombok.Data;

@Data
public class LearningContentResponseDTO {
    private Long id;
    private String title;
    private String description;
    private ContentType contentType;
    private Integer estimatedDurationSeconds;
    private Boolean mandatory;
    private CompletionStatus progress;
    private Long previousContentId;
    private Long nextContentId;
}