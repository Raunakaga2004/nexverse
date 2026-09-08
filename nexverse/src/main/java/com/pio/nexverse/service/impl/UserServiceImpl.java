package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.UserDetailsDTO;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.exception.IncorrectPasswordException;
import com.pio.nexverse.exception.PasswordReuseException;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.FileStorageService;
import com.pio.nexverse.service.OrganizationService;
import com.pio.nexverse.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;
    private final OrganizationService organizationService;
    private final FileStorageService fileStorageService;

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User user = currentUserService.getCurrentUser();
        log.info("Changing password. userId = {}", user.getId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed. Incorrect current password. userId = {}", user.getId());
            throw new IncorrectPasswordException();
        }
        if (oldPassword.equals(newPassword)) {
            log.warn("Password change failed. Password reuse attempted. userId = {}", user.getId());
            throw new PasswordReuseException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully. userId = {}", user.getId());
    }

    @Override
    public UserDetailsDTO getMe() {
        User user = currentUserService.getCurrentUser();
        UserDetailsDTO userDetailsDTO = modelMapper.map(user, UserDetailsDTO.class);
        Organization organization = user.getOrganization();
        if (organization != null) {
            userDetailsDTO.setOrganizationId(organization.getId());
            userDetailsDTO.setOrganizationName(organization.getName());
            userDetailsDTO.setOrganizationLogoUrl(organization.getLogoUrl());
        }
        Department department = user.getDepartment();
        if (department != null) {
            userDetailsDTO.setDepartmentId(department.getId());
            userDetailsDTO.setDepartmentName(department.getName());
        }
        return userDetailsDTO;
    }

    @Override
    public Resource getOrganizationLogo() {
        return organizationService.getOrganizationLogo(currentUserService.getCurrentOrganization().getId());
    }

    @Override
    public Resource getProfileImage() {
        User currentUser = currentUserService.getCurrentUser();
        return fileStorageService.get(currentUser.getProfileImageUrl());
    }
}