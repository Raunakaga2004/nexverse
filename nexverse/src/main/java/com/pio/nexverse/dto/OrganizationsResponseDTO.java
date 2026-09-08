package com.pio.nexverse.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrganizationsResponseDTO {
    private Long id;
    private String name;
    private String logoUrl;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEnabled;
}