package com.pio.nexverse.dto;

import com.pio.nexverse.enums.Role;
import lombok.Data;

@Data
public class UserDetailsDTO {
    private String firstName;
    private String lastName;
    private Role role;
    private String profileImageUrl;
    private Long departmentId;
    private String departmentName;
    private Long organizationId;
    private String organizationName;
    private String organizationLogoUrl; // because not part of info on dashboard
}