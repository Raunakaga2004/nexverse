package com.pio.nexverse.dto;

import com.pio.nexverse.enums.Level;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseSkillRequestDTO {
    private Long skillId;
    private Level skillLevel;
}