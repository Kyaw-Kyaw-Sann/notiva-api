package com.kyawhsan.notiva.service;

import com.kyawhsan.notiva.common.exception.EmbeddingApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PgVectorService {

    private final JdbcTemplate jdbcTemplate;

    public String getInstalledVersion() {
        String version = jdbcTemplate.query("""
                SELECT extversion
                FROM pg_extension
                WHERE extname = 'vector'
                """, resultSet -> resultSet.next() ? resultSet.getString("extversion") : null);

        if (version == null || version.isBlank()) {
            throw new EmbeddingApiException("pgvector extension is not enabled");
        }

        return version;
    }
}