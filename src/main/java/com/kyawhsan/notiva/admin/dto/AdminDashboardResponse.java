package com.kyawhsan.notiva.admin.dto;

public record AdminDashboardResponse(
        long totalUsers,
        long totalNotes,
        long totalAiRequests) {
}
