package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.security.CurrentUserService;

import com.kyawhsan.notiva.config.AiUsageProperties;
import com.kyawhsan.notiva.ai.dto.AiUsageResponse;
import com.kyawhsan.notiva.ai.entity.AiUsage;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.user.enums.UserPlan;
import com.kyawhsan.notiva.common.exception.AiUsageLimitException;
import com.kyawhsan.notiva.ai.repository.AiUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AiUsageService {

    private final AiUsageRepository aiUsageRepository;
    private final AiUsageProperties usageProperties;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public AiUsageResponse getCurrentUsage() {
        User currentUser = currentUserService.getCurrentUser();

        return getUsageResponse(currentUser);
    }

    @Transactional(readOnly = true)
    public void validateUsageAvailable(
            User user) {
        LocalDate usageDate = getCurrentUsageDate();

        int used = aiUsageRepository.findByUserAndUsageDate(user, usageDate)
                .map(AiUsage::getRequestCount).orElse(0);

        int dailyLimit = resolveDailyLimit(user.getPlan());

        if (used >= dailyLimit) {
            throw new AiUsageLimitException("Daily AI usage limit reached");
        }
    }

    @Transactional
    public AiUsageResponse incrementUsage(
            User user) {
        LocalDate usageDate = getCurrentUsageDate();

        AiUsage usage = aiUsageRepository.findByUserAndUsageDate(user, usageDate)
                .orElseGet(() -> createUsage(user, usageDate));

        usage.setRequestCount(usage.getRequestCount() + 1);

        AiUsage savedUsage = aiUsageRepository.save(usage);

        return toResponse(user, savedUsage.getRequestCount(), usageDate);
    }

    private AiUsageResponse getUsageResponse(
            User user) {
        LocalDate usageDate = getCurrentUsageDate();

        int used = aiUsageRepository.findByUserAndUsageDate(user, usageDate)
                .map(AiUsage::getRequestCount).orElse(0);

        return toResponse(user, used, usageDate);
    }

    private AiUsage createUsage(
            User user,
            LocalDate usageDate) {
        AiUsage usage = new AiUsage();

        usage.setUser(user);
        usage.setUsageDate(usageDate);
        usage.setRequestCount(0);

        return usage;
    }

    private AiUsageResponse toResponse(
            User user,
            int used,
            LocalDate usageDate) {
        int dailyLimit = resolveDailyLimit(user.getPlan());

        int remaining = Math.max(dailyLimit - used, 0);

        return new AiUsageResponse(user.getPlan(), dailyLimit, used, remaining, usageDate);
    }

    private int resolveDailyLimit(
            UserPlan plan) {
        if (plan == UserPlan.PREMIUM) {
            return usageProperties.getPremiumDailyLimit();
        }

        return usageProperties.getNormalDailyLimit();
    }

    private LocalDate getCurrentUsageDate() {
        ZoneId zoneId = ZoneId.of(usageProperties.getTimeZone());

        return LocalDate.now(zoneId);
    }
}
