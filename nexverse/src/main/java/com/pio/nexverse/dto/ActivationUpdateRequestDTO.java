package com.pio.nexverse.dto;

import com.pio.nexverse.constants.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivationUpdateRequestDTO {
    @NotNull(message = ValidationMessages.ACTIVATION_UPDATE_FIELD)
    private Boolean isEnabled;
}