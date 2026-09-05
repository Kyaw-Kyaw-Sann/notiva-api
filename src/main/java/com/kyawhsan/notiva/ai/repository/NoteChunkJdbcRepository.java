package com.kyawhsan.notiva.ai.repository;

import com.kyawhsan.notiva.ai.dto.NoteChunkData;
import com.kyawhsan.notiva.ai.dto.SemanticSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoteChunkJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void deleteAllByNoteId(
            Long noteId) {
        jdbcTemplate.update("""
                DELETE FROM note_chunks
                WHERE note_id = ?
                """, noteId);
    }

    public void deleteAllByUserId(
            Long userId) {
        jdbcTemplate.update("""
                DELETE FROM note_chunks
                WHERE user_id = ?
                """, userId);
    }

    public void saveAll(
            Long noteId,
            Long userId,
            List<NoteChunkData> chunks,
            List<float[]> vectors) {
        if (chunks.size() != vectors.size()) {
            throw new IllegalArgumentException("Chunks and vectors count must match");
        }

        List<Object[]> parameters = new ArrayList<>();

        for (int index = 0; index < chunks.size(); index++) {

            NoteChunkData chunk = chunks.get(index);

            parameters.add(new Object[] { chunk.content(), chunk.chunkIndex(),
                    toVectorString(vectors.get(index)), noteId, userId });
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO note_chunks (
                    content,
                    chunk_index,
                    embedding,
                    note_id,
                    user_id
                )
                VALUES (?, ?, ?::vector, ?, ?)
                """, parameters);
    }

    public List<SemanticSearchResult> findMostSimilarActiveChunks(
            Long userId,
            float[] queryVector,
            int limit) {
        String vector = toVectorString(queryVector);

        return jdbcTemplate.query("""
                SELECT
                    nc.id AS chunk_id,
                    nc.note_id,
                    n.title AS note_title,
                    nc.chunk_index,
                    nc.content,
                    1 - (
                        nc.embedding <=> ?::vector
                    ) AS similarity
                FROM note_chunks nc
                INNER JOIN notes n
                    ON n.id = nc.note_id
                WHERE nc.user_id = ?
                  AND n.user_id = ?
                  AND n.deleted_at IS NULL
                ORDER BY
                    nc.embedding <=> ?::vector
                LIMIT ?
                """, (
                resultSet,
                rowNumber) -> new SemanticSearchResult(resultSet.getLong("chunk_id"),
                        resultSet.getLong("note_id"), resultSet.getString("note_title"),
                        resultSet.getInt("chunk_index"), resultSet.getString("content"),
                        resultSet.getDouble("similarity")),
                vector, userId, userId, vector, limit);
    }

    public long countByNoteId(
            Long noteId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM note_chunks
                WHERE note_id = ?
                """, Long.class, noteId);

        return count == null ? 0 : count;
    }

    private String toVectorString(
            float[] vector) {
        StringBuilder builder = new StringBuilder("[");

        for (int index = 0; index < vector.length; index++) {

            if (index > 0) {
                builder.append(',');
            }

            builder.append(vector[index]);
        }

        builder.append(']');

        return builder.toString();
    }
}
