package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.ai.dto.NoteChunkData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NoteChunker {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 150;

    public List<NoteChunkData> chunk(
            String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return List.of();
        }

        String content = plainText.trim();

        if (content.length() <= CHUNK_SIZE) {
            return List.of(new NoteChunkData(0, content));
        }

        List<NoteChunkData> chunks = new ArrayList<>();

        int start = 0;
        int chunkIndex = 0;

        while (start < content.length()) {
            int end = Math.min(start + CHUNK_SIZE, content.length());

            String chunk = content.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(new NoteChunkData(chunkIndex, chunk));

                chunkIndex++;
            }

            if (end >= content.length()) {
                break;
            }

            start = end - CHUNK_OVERLAP;
        }

        return chunks;
    }
}
