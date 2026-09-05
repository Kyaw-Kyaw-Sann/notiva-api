package com.kyawhsan.notiva.ai.controller;

import com.kyawhsan.notiva.ai.dto.AiGenerateRequest;
import com.kyawhsan.notiva.ai.dto.AiGenerateResponse;
import com.kyawhsan.notiva.ai.dto.AiUsageResponse;
import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.ai.service.AiService;
import com.kyawhsan.notiva.ai.service.AiUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiUsageService aiUsageService;

    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<AiUsageResponse>> getCurrentUsage() {
        AiUsageResponse response = aiUsageService.getCurrentUsage();

        return ResponseEntity.ok(ApiResponse.success("AI usage retrieved successfully", response));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AiGenerateResponse>> generate(
            @Valid
            @RequestBody
            AiGenerateRequest request) {
        AiGenerateResponse response = aiService.generate(request);

        return ResponseEntity
                .ok(ApiResponse.success("AI response generated successfully", response));
    }
}
