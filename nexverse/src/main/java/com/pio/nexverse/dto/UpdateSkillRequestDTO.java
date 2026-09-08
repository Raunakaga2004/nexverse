package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateSkillRequestDTO {
    private String name;
    private String description;
    private String iconUrl;
}