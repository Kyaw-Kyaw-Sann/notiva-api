package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.enums.UserPlan;

import java.time.LocalDate;

public record AiUsageResponse(

        UserPlan plan,

        int dailyLimit,

        int used,

        int remaining,

        LocalDate usageDate

) {
}
