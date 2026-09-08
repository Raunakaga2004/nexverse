package com.pio.nexverse.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseModuleViewDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private String description;
    private Integer sequenceOrder;
    private Integer estimatedDurationSeconds;
    private Integer totalContents;
    private List<ModuleContentViewDTO> contents;
}