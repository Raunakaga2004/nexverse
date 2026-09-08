package com.pio.nexverse.dto;

import lombok.Data;

import java.util.List;

@Data
public class LearningCourseResponseDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private Integer estimatedDurationSeconds;
    private Integer totalContents;
    private Integer completedContents;
    private Double progressPercent;
    private List<LearningModuleResponseDTO> modules;
}
