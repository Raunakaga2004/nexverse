package com.pio.nexverse.dto;

import com.pio.nexverse.enums.Level;
import lombok.Data;
import lombok.Builder;

@Builder
@Data
public class EmployeeSkillLevelResponseDTO {
    private Level level;
    private Long courseCount;
}