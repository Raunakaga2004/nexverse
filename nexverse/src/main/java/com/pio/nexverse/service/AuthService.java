package com.pio.nexverse.service;

import com.pio.nexverse.dto.LoginRequestDTO;
import com.pio.nexverse.dto.TokenPair;

public interface AuthService {
    TokenPair login(LoginRequestDTO request);

    TokenPair refreshToken(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(String email);

    void setPassword(String newPassword, String token);
}