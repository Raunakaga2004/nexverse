package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentOrderDTO {
    private Long contentId;
    private Integer sequenceOrder;
}
