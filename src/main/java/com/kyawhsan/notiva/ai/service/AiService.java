package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.security.CurrentUserService;

import com.kyawhsan.notiva.ai.dto.AiGenerateRequest;
import com.kyawhsan.notiva.ai.dto.AiGenerateResponse;
import com.kyawhsan.notiva.ai.dto.AiUsageResponse;
import com.kyawhsan.notiva.ai.dto.GroqGenerationResult;
import com.kyawhsan.notiva.ai.client.GroqClient;
import com.kyawhsan.notiva.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final GroqClient groqClient;
    private final AiUsageService aiUsageService;
    private final CurrentUserService currentUserService;

    public AiGenerateResponse generate(
            AiGenerateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        aiUsageService.validateUsageAvailable(currentUser);

        GroqGenerationResult result = groqClient.generate(request.prompt());

        AiUsageResponse updatedUsage = aiUsageService.incrementUsage(currentUser);

        return new AiGenerateResponse(result.content(), result.model(), result.promptTokens(),
                result.completionTokens(), result.totalTokens(), updatedUsage);
    }
}
