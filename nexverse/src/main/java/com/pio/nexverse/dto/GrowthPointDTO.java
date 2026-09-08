package com.pio.nexverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrowthPointDTO {
    private Integer year;
    private Integer month;
    private Long count;
}