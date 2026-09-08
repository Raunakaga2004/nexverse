package com.pio.nexverse.service;

import com.pio.nexverse.dto.UserDetailsDTO;
import org.springframework.core.io.Resource;

public interface UserService {
    void changePassword(String oldPassword, String newPassword);

    UserDetailsDTO getMe();

    Resource getOrganizationLogo();

    Resource getProfileImage();
}