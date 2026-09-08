package com.pio.nexverse.config;

import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.enums.UserStatus;
import com.pio.nexverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.email}")
    private String email;

    @Value("${app.super-admin.password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(userRepository.existsByRoleAndIsEnabledTrue(Role.SUPER_ADMIN)){
            return;
        }
        User superAdmin = new User();
        superAdmin.setFirstName("Super Admin");
        superAdmin.setEmail(email);
        superAdmin.setPhoneNumber("9999999999");
        superAdmin.setPassword(passwordEncoder.encode(password));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setStatus(UserStatus.ACTIVE);
        userRepository.save(superAdmin);
    }
}