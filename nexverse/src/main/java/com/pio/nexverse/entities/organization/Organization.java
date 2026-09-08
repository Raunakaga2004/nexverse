package com.pio.nexverse.entities.organization;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.OrganizationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "organization")
public class Organization extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrganizationStatus status;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();
}