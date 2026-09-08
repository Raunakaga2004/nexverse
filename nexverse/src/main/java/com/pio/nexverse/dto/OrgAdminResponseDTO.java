package com.pio.nexverse.dto;

import com.pio.nexverse.enums.UserStatus;
import lombok.Data;

@Data
public class OrgAdminResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserStatus status;
}