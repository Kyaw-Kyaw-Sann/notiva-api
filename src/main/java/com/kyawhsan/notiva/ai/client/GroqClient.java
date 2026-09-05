package com.kyawhsan.notiva.ai.client;

import com.kyawhsan.notiva.config.GroqProperties;
import com.kyawhsan.notiva.ai.dto.GroqChatRequest;
import com.kyawhsan.notiva.ai.dto.GroqChatResponse;
import com.kyawhsan.notiva.ai.dto.GroqChoice;
import com.kyawhsan.notiva.ai.dto.GroqGenerationResult;
import com.kyawhsan.notiva.ai.dto.GroqMessage;
import com.kyawhsan.notiva.ai.dto.GroqUsage;
import com.kyawhsan.notiva.common.exception.GroqApiException;
import com.kyawhsan.notiva.common.exception.GroqTimeoutException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.SocketTimeoutException;
import java.util.List;

@Service
public class GroqClient {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final String SYSTEM_PROMPT = """
            You are Notiva's helpful AI note assistant.
            Answer clearly and accurately.
            When the user writes in Myanmar language, respond in Myanmar language.
            Do not claim access to notes that were not included in the prompt.
            """;

    private final RestClient groqRestClient;
    private final GroqProperties groqProperties;

    public GroqClient(
            @Qualifier("groqRestClient")
            RestClient groqRestClient,
            GroqProperties groqProperties) {
        this.groqRestClient = groqRestClient;
        this.groqProperties = groqProperties;
    }

    public GroqGenerationResult generate(
            String prompt) {
        return generate(SYSTEM_PROMPT, prompt);
    }

    public GroqGenerationResult generate(
            String systemPrompt,
            String userPrompt) {
        return generate(List.of(new GroqMessage("system", systemPrompt.trim()),
                new GroqMessage("user", userPrompt.trim())));
    }

    public GroqGenerationResult generate(
            List<GroqMessage> messages) {
        validateConfiguration();

        if (messages == null || messages.isEmpty()) {
            throw new GroqApiException("Groq messages are required");
        }

        GroqChatRequest request = new GroqChatRequest(groqProperties.getModel(), messages,
                groqProperties.getTemperature(), groqProperties.getMaxTokens(), false);

        try {
            GroqChatResponse response = groqRestClient.post().uri(CHAT_COMPLETIONS_PATH)
                    .body(request).retrieve().body(GroqChatResponse.class);

            return mapResponse(response);

        } catch (ResourceAccessException exception) {
            throw createResourceAccessException(exception);

        } catch (RestClientResponseException exception) {
            throw createResponseException(exception);

        } catch (RestClientException exception) {
            throw new GroqApiException("Unable to communicate with Groq", exception);
        }
    }

    private GroqGenerationResult mapResponse(
            GroqChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new GroqApiException("Groq returned an empty response");
        }

        GroqChoice firstChoice = response.choices().getFirst();

        if (firstChoice.message() == null || firstChoice.message().content() == null
                || firstChoice.message().content().isBlank()) {
            throw new GroqApiException("Groq returned empty response content");
        }

        GroqUsage usage = response.usage();

        return new GroqGenerationResult(firstChoice.message().content(), response.model(),
                usage == null ? null : usage.prompt_tokens(),
                usage == null ? null : usage.completion_tokens(),
                usage == null ? null : usage.total_tokens());
    }

    private RuntimeException createResourceAccessException(
            ResourceAccessException exception) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(exception);

        if (rootCause instanceof SocketTimeoutException) {
            return new GroqTimeoutException("Groq request timed out", exception);
        }

        return new GroqApiException("Unable to reach Groq service", exception);
    }

    private GroqApiException createResponseException(
            RestClientResponseException exception) {
        int status = exception.getStatusCode().value();

        if (status == 401) {
            return new GroqApiException("Groq API key is invalid or unauthorized", exception);
        }

        if (status == 403) {
            return new GroqApiException("Groq API access is forbidden", exception);
        }

        if (status == 429) {
            return new GroqApiException("Groq provider rate limit reached", exception);
        }

        if (status == 400) {
            return new GroqApiException("Groq rejected the AI request", exception);
        }

        if (status >= 500) {
            return new GroqApiException("Groq service is temporarily unavailable", exception);
        }

        return new GroqApiException("Unable to complete the Groq request", exception);
    }

    private void validateConfiguration() {
        String apiKey = groqProperties.getApiKey();

        if (apiKey == null || apiKey.isBlank() || apiKey.equals("${GROQ_API_KEY}")) {
            throw new GroqApiException("Groq API key is not configured");
        }

        if (groqProperties.getModel() == null || groqProperties.getModel().isBlank()) {
            throw new GroqApiException("Groq model is not configured");
        }
    }
}
