package com.pio.nexverse.dto;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Builder
@Data
public class EmployeeSkillResponseDTO {
    private Long skillId;
    private String skillName;
    private List<EmployeeSkillLevelResponseDTO> levels;
}