package com.kyawhsan.notiva.note.service;

import com.kyawhsan.notiva.common.response.ImageUploadResponse;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.common.util.ImageValidator;
import com.kyawhsan.notiva.common.storage.CloudinaryService;
import com.kyawhsan.notiva.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NoteImageService {

    private static final String NOTE_IMAGE_FOLDER = "notiva/note-images";

    private final CurrentUserService currentUserService;
    private final CloudinaryService cloudinaryService;
    private final ImageValidator imageValidator;

    @Value("${app.image.note-max-size}")
    private long noteImageMaximumSize;

    public ImageUploadResponse uploadImage(
            MultipartFile file) {
        imageValidator.validate(file, noteImageMaximumSize);

        User user = currentUserService.getCurrentUser();
        String userFolder = buildUserFolder(user.getId());

        return cloudinaryService.uploadImage(file, userFolder);
    }

    public void deleteImage(
            String publicId) {
        User user = currentUserService.getCurrentUser();
        String userFolder = buildUserFolder(user.getId());

        validateImageOwnership(publicId, userFolder);

        cloudinaryService.deleteImage(publicId);
    }

    private String buildUserFolder(
            Long userId) {
        return NOTE_IMAGE_FOLDER + "/user-" + userId;
    }

    private void validateImageOwnership(
            String publicId,
            String userFolder) {
        if (publicId == null || publicId.isBlank() || !publicId.startsWith(userFolder + "/")) {

            throw new BadRequestException("Invalid note image public ID");
        }
    }
}
