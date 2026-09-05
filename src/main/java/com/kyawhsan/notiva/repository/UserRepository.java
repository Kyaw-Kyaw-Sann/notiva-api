package com.kyawhsan.notiva.repository;

import com.kyawhsan.notiva.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(
            String email);

    Optional<User> findByGoogleId(
            String googleId);

    boolean existsByEmailIgnoreCase(
            String email);

    boolean existsByGoogleId(
            String googleId);

    Page<User> findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String email,
            String displayName,
            Pageable pageable);
}
