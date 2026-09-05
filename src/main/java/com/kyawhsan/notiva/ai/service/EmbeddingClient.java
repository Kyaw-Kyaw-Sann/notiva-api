package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.config.EmbeddingProperties;
import com.kyawhsan.notiva.ai.dto.EmbeddingResult;
import com.kyawhsan.notiva.ai.dto.VoyageEmbeddingData;
import com.kyawhsan.notiva.ai.dto.VoyageEmbeddingRequest;
import com.kyawhsan.notiva.ai.dto.VoyageEmbeddingResponse;
import com.kyawhsan.notiva.ai.dto.VoyageEmbeddingUsage;
import com.kyawhsan.notiva.ai.enums.EmbeddingInputType;
import com.kyawhsan.notiva.common.exception.EmbeddingApiException;
import com.kyawhsan.notiva.common.exception.EmbeddingTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.kyawhsan.notiva.ai.dto.EmbeddingBatchResult;

import java.util.Comparator;

import java.util.List;

@Service
public class EmbeddingClient {

    private static final String EMBEDDINGS_PATH = "/embeddings";

    private final RestClient voyageRestClient;
    private final EmbeddingProperties properties;

    public EmbeddingClient(
            @Qualifier("voyageRestClient")
            RestClient voyageRestClient,
            EmbeddingProperties properties) {
        this.voyageRestClient = voyageRestClient;
        this.properties = properties;
    }

    public EmbeddingResult embedDocument(
            String text) {
        return generateEmbedding(text, EmbeddingInputType.DOCUMENT);
    }

    public EmbeddingResult embedQuery(
            String text) {
        return generateEmbedding(text, EmbeddingInputType.QUERY);
    }

    public EmbeddingBatchResult embedDocuments(
            List<String> texts) {
        validateConfiguration();

        if (texts == null || texts.isEmpty()) {
            throw new EmbeddingApiException("Embedding texts are required");
        }

        List<String> cleanedTexts = texts.stream().map(text -> {
            validateText(text);
            return text.trim();
        }).toList();

        VoyageEmbeddingRequest request = new VoyageEmbeddingRequest(cleanedTexts,
                properties.getModel(), EmbeddingInputType.DOCUMENT.getApiValue(), true,
                properties.getDimension(), "float");

        try {
            VoyageEmbeddingResponse response = voyageRestClient.post().uri(EMBEDDINGS_PATH)
                    .body(request).retrieve().body(VoyageEmbeddingResponse.class);

            return mapBatchResponse(response, cleanedTexts.size());

        } catch (ResourceAccessException exception) {
            throw new EmbeddingTimeoutException("Embedding request timed out", exception);

        } catch (HttpClientErrorException exception) {
            throw createClientException(exception);

        } catch (HttpServerErrorException exception) {
            throw new EmbeddingApiException("Embedding service is temporarily unavailable",
                    exception);

        } catch (RestClientException exception) {
            throw new EmbeddingApiException("Unable to communicate with embedding service",
                    exception);
        }
    }

    private EmbeddingBatchResult mapBatchResponse(
            VoyageEmbeddingResponse response,
            int expectedSize) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new EmbeddingApiException("Embedding service returned an empty response");
        }

        if (response.data().size() != expectedSize) {
            throw new EmbeddingApiException("Embedding result count does not match input count");
        }

        List<VoyageEmbeddingData> sortedData = response.data().stream()
                .sorted(Comparator.comparingInt(VoyageEmbeddingData::index)).toList();

        List<float[]> vectors = sortedData.stream().map(data -> {
            if (data.embedding() == null || data.embedding().size() != properties.getDimension()) {
                throw new EmbeddingApiException("Embedding dimension does not match configuration");
            }

            return convertToFloatArray(data.embedding());
        }).toList();

        VoyageEmbeddingUsage usage = response.usage();

        return new EmbeddingBatchResult(vectors, response.model(),
                usage == null ? 0 : usage.total_tokens());
    }

    private EmbeddingResult generateEmbedding(
            String text,
            EmbeddingInputType inputType) {
        validateConfiguration();
        validateText(text);

        VoyageEmbeddingRequest request = new VoyageEmbeddingRequest(List.of(text.trim()),
                properties.getModel(), inputType.getApiValue(), true, properties.getDimension(),
                "float");

        try {
            VoyageEmbeddingResponse response = voyageRestClient.post().uri(EMBEDDINGS_PATH)
                    .body(request).retrieve().body(VoyageEmbeddingResponse.class);

            return mapResponse(response);

        } catch (ResourceAccessException exception) {
            throw new EmbeddingTimeoutException("Embedding request timed out", exception);

        } catch (HttpClientErrorException exception) {
            throw createClientException(exception);

        } catch (HttpServerErrorException exception) {
            throw new EmbeddingApiException("Embedding service is temporarily unavailable",
                    exception);

        } catch (RestClientException exception) {
            throw new EmbeddingApiException("Unable to communicate with embedding service",
                    exception);
        }
    }

    private EmbeddingResult mapResponse(
            VoyageEmbeddingResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new EmbeddingApiException("Embedding service returned an empty response");
        }

        VoyageEmbeddingData firstResult = response.data().getFirst();

        if (firstResult.embedding() == null || firstResult.embedding().isEmpty()) {
            throw new EmbeddingApiException("Embedding service returned an empty vector");
        }

        if (firstResult.embedding().size() != properties.getDimension()) {
            throw new EmbeddingApiException("Embedding dimension does not match configuration");
        }

        float[] vector = convertToFloatArray(firstResult.embedding());

        VoyageEmbeddingUsage usage = response.usage();

        return new EmbeddingResult(vector, response.model(),
                usage == null ? 0 : usage.total_tokens());
    }

    private float[] convertToFloatArray(
            List<Double> values) {
        float[] vector = new float[values.size()];

        for (int index = 0; index < values.size(); index++) {
            vector[index] = values.get(index).floatValue();
        }

        return vector;
    }

    private EmbeddingApiException createClientException(
            HttpClientErrorException exception) {
        HttpStatusCode status = exception.getStatusCode();

        if (status.value() == 401) {
            return new EmbeddingApiException("Voyage API key is invalid or unauthorized",
                    exception);
        }

        if (status.value() == 403) {
            return new EmbeddingApiException("Voyage API access is forbidden", exception);
        }

        if (status.value() == 429) {
            return new EmbeddingApiException("Voyage provider rate limit reached", exception);
        }

        if (status.value() == 400) {
            return new EmbeddingApiException("Voyage rejected the embedding request", exception);
        }

        return new EmbeddingApiException("Unable to complete the embedding request", exception);
    }

    private void validateConfiguration() {
        String apiKey = properties.getApiKey();

        if (apiKey == null || apiKey.isBlank() || apiKey.equals("${VOYAGE_API_KEY}")) {
            throw new EmbeddingApiException("Voyage API key is not configured");
        }

        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new EmbeddingApiException("Voyage base URL is not configured");
        }

        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new EmbeddingApiException("Embedding model is not configured");
        }

        if (properties.getDimension() <= 0) {
            throw new EmbeddingApiException("Embedding dimension is invalid");
        }
    }

    private void validateText(
            String text) {
        if (text == null || text.isBlank()) {
            throw new EmbeddingApiException("Embedding text is required");
        }
    }
}
