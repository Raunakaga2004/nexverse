package com.pio.nexverse.security;

import com.pio.nexverse.entities.auth.RefreshToken;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream().toList());
        String token = buildToken(claims, userDetails.getUsername(), new Date(System.currentTimeMillis() + accessTokenExpiration));
        log.info("Access token generated successfully. username = {}", userDetails.getUsername());
        return token;
    }

    public String generateRefreshToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>();
        Date expirationTime = new Date(System.currentTimeMillis() + refreshTokenExpiration);
        LocalDateTime refreshTokenExpiry = LocalDateTime.ofInstant(expirationTime.toInstant(), ZoneId.systemDefault());
        String token = buildToken(claims, userDetails.getUsername(), expirationTime);
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId()).orElse(null);
        if (refreshToken != null) {
            refreshToken.setToken(token);
            refreshToken.setExpiresAt(refreshTokenExpiry);
        } else {
            refreshToken = new RefreshToken();
            refreshToken.setToken(token);
            refreshToken.setUser(user);
            refreshToken.setExpiresAt(refreshTokenExpiry);
        }
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token generated successfully. userId = {}", user.getId());
        return token;
    }

    private String buildToken(Map<String, Object> claims, String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(getSigningWith())
                .compact();
    }

    public String validateAndExtractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningWith())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningWith() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}