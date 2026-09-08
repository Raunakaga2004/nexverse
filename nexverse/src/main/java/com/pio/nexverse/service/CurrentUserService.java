package com.pio.nexverse.service;

import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;

public interface CurrentUserService {
    User getCurrentUser();

    Organization getCurrentOrganization();

    Department getCurrentDepartment();
}