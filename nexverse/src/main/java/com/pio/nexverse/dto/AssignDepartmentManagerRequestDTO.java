package com.pio.nexverse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignDepartmentManagerRequestDTO {
    @NotNull
    Long employeeId;
}