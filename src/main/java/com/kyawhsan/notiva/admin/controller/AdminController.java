package com.kyawhsan.notiva.admin.controller;

import com.kyawhsan.notiva.admin.dto.AdminDashboardResponse;
import com.kyawhsan.notiva.admin.dto.AdminUserResponse;
import com.kyawhsan.notiva.admin.service.AdminService;
import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.common.response.PagedResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard retrieved successfully",
                adminService.dashboard()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> users(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Admin users retrieved successfully",
                adminService.users(search, page, size)));
    }
}
