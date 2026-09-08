package com.pio.nexverse.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseAccessRequestDTO {
    @Size(max=500)
    private String reason;
}