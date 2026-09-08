package com.pio.nexverse.service.impl;

import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.exception.DepartmentDisableException;
import com.pio.nexverse.exception.OrganizationDisabledException;
import com.pio.nexverse.exception.UserNotFoundException;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {
    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String email = principal.getUsername();
        return userRepository.findByEmailIgnoreCaseAndIsEnabledTrue(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public Organization getCurrentOrganization() {
        Organization organization = getCurrentUser().getOrganization();
        if (!organization.isEnabled()) {
            throw new OrganizationDisabledException();
        }
        return organization;
    }

    @Override
    public Department getCurrentDepartment() {
        Department department = getCurrentUser().getDepartment();
        if (!department.isEnabled()) {
            throw new DepartmentDisableException();
        }
        return department;
    }
}