package com.kyawhsan.notiva.auth.repository;

import com.kyawhsan.notiva.auth.entity.AuthToken;
import com.kyawhsan.notiva.entity.User;
import com.kyawhsan.notiva.auth.enums.AuthTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

        Optional<AuthToken> findByTokenAndType(
                        String token,
                        AuthTokenType type);

        Optional<AuthToken> findFirstByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(
                        User user,
                        AuthTokenType type);

        boolean existsByUserAndTypeAndUsedFalse(
                        User user,
                        AuthTokenType type);

        void deleteAllByUserAndType(
                        User user,
                        AuthTokenType type);

        void deleteAllByUser(
                        User user);

        void deleteAllByExpiresAtBefore(
                        LocalDateTime dateTime);

}