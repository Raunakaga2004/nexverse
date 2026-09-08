package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModuleOrderDTO {
    Long moduleId;
    Integer sequenceOrder;
}
