package com.pio.nexverse.entities.auth;

import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserSetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    private boolean isTokenUsed;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public UserSetPasswordToken(String token, LocalDateTime expiresAt, TokenType type, User user){
        this.token = token;
        this.expiresAt = expiresAt;
        this.type = type;
        this.isTokenUsed = false;
        this.user = user;
    }
}