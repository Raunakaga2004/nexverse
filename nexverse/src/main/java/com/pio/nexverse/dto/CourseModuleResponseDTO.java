package com.pio.nexverse.dto;

import com.pio.nexverse.enums.ResourceCreationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseModuleResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Integer sequenceOrder;
    private Boolean isMandatory;
    private Integer estimatedDurationSeconds;
    private Integer numberOfContents;
    private boolean hasAccess;
    private ResourceCreationStatus courseStatus;
    private List<ModuleContentResponseDTO> contents;
}