package com.pio.nexverse.repository;

import com.pio.nexverse.entities.auth.UserSetPasswordToken;
import com.pio.nexverse.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSetPasswordRepository extends JpaRepository<UserSetPasswordToken, Long> {
    void deleteByUser(User user);
    Optional<UserSetPasswordToken> findByToken(String token);

    Optional<UserSetPasswordToken> findByUserId(Long userId);
}