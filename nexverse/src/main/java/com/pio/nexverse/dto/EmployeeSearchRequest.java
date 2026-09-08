package com.pio.nexverse.dto;

import com.pio.nexverse.enums.Role;
import com.pio.nexverse.enums.UserStatus;
import lombok.Data;

@Data
public class EmployeeSearchRequest {
    private UserStatus status;
    private Role role;
    private String departmentName;
    private String search;
    private Boolean isEnabled;
}