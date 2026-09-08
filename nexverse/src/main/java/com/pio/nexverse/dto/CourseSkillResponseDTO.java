package com.pio.nexverse.dto;

import com.pio.nexverse.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSkillResponseDTO {
    private Long id;
    private SkillResponseDTO skill;
    private Level skillLevel;
}
