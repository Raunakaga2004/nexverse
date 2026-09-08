package com.pio.nexverse.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReorderModuleContentsRequestDTO {
    @NotEmpty
    private List<ContentOrderDTO> contents;
}