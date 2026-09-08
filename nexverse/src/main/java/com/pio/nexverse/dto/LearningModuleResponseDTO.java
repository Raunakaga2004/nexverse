package com.pio.nexverse.dto;

import lombok.Data;
import java.util.List;

@Data
public class LearningModuleResponseDTO {
    private Long id;
    private String title;
    private Integer sequenceOrder;
    private Integer totalContents;
    private Integer completedContents;
    private Double progressPercent;
    private List<LearningContentItemResponseDTO> contents;
}