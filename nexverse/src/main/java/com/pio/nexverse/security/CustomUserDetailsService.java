package com.pio.nexverse.security;

import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.OrganizationStatus;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.enums.UserStatus;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Authentication attempt for email = {}", email);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> {
            log.warn("Authentication failed. User not found. email = {}", email);
            return new UsernameNotFoundException("User not found");
        });
        if (!user.isEnabled()) {
            log.warn("Authentication failed. User account is disabled. userId = {}", user.getId());
            throw new UserAccountDisabledException();
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Authentication failed. User suspended. userId = {}", user.getId());
            throw new UserSuspendedException();
        }
        Organization organization = user.getOrganization();
        if (organization != null) {
            if (!organization.isEnabled()) {
                log.warn("Authentication failed. Organization is disabled. organizationId = {}", organization.getId());
                throw new OrganizationDisabledException();
            }
            if (organization.getStatus() == OrganizationStatus.SUSPENDED) {
                log.warn("Authentication failed. Organization is suspended. organizationId = {}", organization.getId());
                throw new OrganizationSuspendedException();
            }
        }
        Role role = user.getRole();
        if(role == Role.MANAGER || role == Role.EMPLOYEE){
            Department department = user.getDepartment();
            if(!department.isEnabled()){
                log.warn("Authentication failed. Department is disabled. departmentId = {}", department.getId());
                throw new DepartmentDisableException();
            }
        }
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().name());
        log.info("Authentication successful. userId = {}", user.getId());
        return org.springframework.security.core.userdetails.User.builder()
                .authorities(grantedAuthority)
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}