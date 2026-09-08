package com.pio.nexverse.dto;

import lombok.Data;

@Data
public class SkillSearchRequest {
    private String search;
    private Boolean isEnabled;
}