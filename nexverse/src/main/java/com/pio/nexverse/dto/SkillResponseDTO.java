package com.pio.nexverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String createdAt;
    private String updatedAt;
    private boolean isEnabled;
}