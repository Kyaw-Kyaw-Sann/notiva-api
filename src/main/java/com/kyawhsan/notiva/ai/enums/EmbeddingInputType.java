package com.kyawhsan.notiva.ai.enums;

public enum EmbeddingInputType {

    DOCUMENT("document"), QUERY("query");

    private final String apiValue;

    EmbeddingInputType(
            String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }
}