package com.kyawhsan.notiva.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.usage")
public class AiUsageProperties {

    private int normalDailyLimit;
    private int premiumDailyLimit;
    private String timeZone;

    public int getNormalDailyLimit() {
        return normalDailyLimit;
    }

    public void setNormalDailyLimit(
            int normalDailyLimit) {
        this.normalDailyLimit = normalDailyLimit;
    }

    public int getPremiumDailyLimit() {
        return premiumDailyLimit;
    }

    public void setPremiumDailyLimit(
            int premiumDailyLimit) {
        this.premiumDailyLimit = premiumDailyLimit;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(
            String timeZone) {
        this.timeZone = timeZone;
    }
}