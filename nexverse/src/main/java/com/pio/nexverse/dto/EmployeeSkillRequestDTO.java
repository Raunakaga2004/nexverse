package com.pio.nexverse.dto;

import com.pio.nexverse.enums.Level;
import lombok.Data;

@Data
public class EmployeeSkillRequestDTO {
    private String skillName;
    private Level skillLevel;
}