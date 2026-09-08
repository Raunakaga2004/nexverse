package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.LoginRequestDTO;
import com.pio.nexverse.dto.TokenPair;
import com.pio.nexverse.entities.auth.UserSetPasswordToken;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.OrganizationStatus;
import com.pio.nexverse.enums.Role;
import com.pio.nexverse.enums.TokenType;
import com.pio.nexverse.enums.UserStatus;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.RefreshTokenRepository;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.repository.UserSetPasswordRepository;
import com.pio.nexverse.security.JwtService;
import com.pio.nexverse.service.AuthService;
import com.pio.nexverse.service.EmailService;
import com.pio.nexverse.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSetPasswordRepository userSetPasswordRepository;
    private final EmailService emailService;

    public TokenPair login(LoginRequestDTO request) {
        log.info("Authenticating user. email = {}", request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElseThrow(() -> {
            log.warn("Authentication failed. Invalid credentials. email = {}", request.getEmail());
            return new InvalidCredentialsException();
        });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed. Invalid credentials. email = {}", request.getEmail());
            throw new InvalidCredentialsException();
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        checkPendingStatus(user);
        log.info("Authentication successfully. userId = {}", user.getId());
        return generateTokenPair(userDetails, user);
    }

    @Transactional
    public TokenPair refreshToken(String refreshToken) {
        log.info("Refreshing access token.");
        String email = jwtService.validateAndExtractUsername(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);
        userValidation(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        log.info("Access token refreshed successfully. userId = {}", user.getId());
        return generateTokenPair(userDetails, user);
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("Processing logout.");
        refreshTokenRepository.deleteByToken(refreshToken);
        log.info("Logout completed.");
    }

    @Transactional
    public void forgotPassword(String email) {
        log.info("Password reset requested. email = {}", email);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);
        userValidation(user);
        organizationValidation(user.getOrganization());
        userSetPasswordRepository.deleteByUser(user);
        userSetPasswordRepository.flush(); // sending pending queries to db (but doesn't commit)
        String token = TokenUtils.generatePasswordSetToken();
        UserSetPasswordToken userSetPasswordToken = new UserSetPasswordToken(TokenUtils.hashToken(token), LocalDateTime.now().plusDays(1), TokenType.PASSWORD_RESET, user);
        userSetPasswordRepository.save(userSetPasswordToken);
        emailService.sendPasswordResetEmail(email, token);
        log.info("Password reset email sent. userId = {}", user.getId());
    }

    @Transactional
    public void setPassword(String newPassword, String token) {
        log.info("Processing password setup request.");
        UserSetPasswordToken resetToken = userSetPasswordRepository.findByToken(TokenUtils.hashToken(token)).orElseThrow(InvalidSetPasswordTokenException::new);
        if (resetToken.getType() == TokenType.PASSWORD_RESET) {
            if (resetToken.isTokenUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Password setup failed. Invalid or expired token.");
                throw new InvalidSetPasswordTokenException();
            }
        }
        User user = resetToken.getUser();
        userValidation(user);
        organizationValidation(user.getOrganization());
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("Password setup failed. Password reuse detected. userId = {}", user.getId());
            throw new PasswordReuseException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
            if (user.getRole() == Role.ADMIN) {
                Organization organization = user.getOrganization();
                if (organization == null) {
                    throw new OrganizationNotFoundException();
                }
                organization.setStatus(OrganizationStatus.ACTIVE);
            }
        }
        resetToken.setTokenUsed(true);
        userSetPasswordRepository.save(resetToken);
        userRepository.save(user);
        log.info("Password updated successfully. userId = {}", user.getId());
    }

    private void userValidation(User user) {
        if (!user.isEnabled()) {
            log.warn("Authentication failed. User account is disabled. userId = {}", user.getId());
            throw new UserAccountDisabledException();
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            log.warn("Authentication failed. User suspended. userId = {}", user.getId());
            throw new UserSuspendedException();
        }
    }

    private void checkPendingStatus(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new UserPendingException();
        }
        Organization organization = user.getOrganization();
        if (organization != null && organization.getStatus() == OrganizationStatus.PENDING) {
            throw new OrganizationPendingException();
        }
    }

    private void organizationValidation(Organization organization) {
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
    }

    private TokenPair generateTokenPair(UserDetails userDetails, User user) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails, user);
        return TokenPair.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }
}