package com.kyawhsan.notiva.repository;

import com.kyawhsan.notiva.entity.AiUsage;
import com.kyawhsan.notiva.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {

        Optional<AiUsage> findByUserAndUsageDate(
                        User user,
                        LocalDate usageDate);

        void deleteAllByUser(
                        User user);
}