package com.pio.nexverse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import static com.pio.nexverse.constants.ValidationMessages.SKILL_NAME_REQUIRED;

@Data
@Builder
public class CreateSkillRequestDTO {
    @NotBlank(message = SKILL_NAME_REQUIRED)
    private String name;
    private String description;
}