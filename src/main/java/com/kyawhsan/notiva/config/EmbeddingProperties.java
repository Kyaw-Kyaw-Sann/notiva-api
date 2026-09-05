package com.kyawhsan.notiva.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.embedding")
public class EmbeddingProperties {

    private String apiKey;
    private String baseUrl;
    private String model;
    private int dimension;
    private int connectTimeoutSeconds;
    private int readTimeoutSeconds;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(
            String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(
            String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(
            String model) {
        this.model = model;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(
            int dimension) {
        this.dimension = dimension;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(
            int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(
            int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }
}