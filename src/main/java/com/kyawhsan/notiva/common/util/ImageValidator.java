package com.kyawhsan.notiva.common.util;

import com.kyawhsan.notiva.common.exception.InvalidImageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ImageValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png",
            "image/webp");

    public void validate(
            MultipartFile file,
            long maximumSize) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Image file is required");
        }

        if (file.getSize() > maximumSize) {
            throw new InvalidImageException("Image file exceeds the allowed size");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new InvalidImageException("Only JPEG, PNG and WebP images are allowed");
        }
    }
}