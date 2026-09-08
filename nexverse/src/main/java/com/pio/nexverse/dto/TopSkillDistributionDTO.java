package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopSkillDistributionDTO {
    private Long skillId;
    private String skillName;
    private long courseCount;
}