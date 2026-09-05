package com.kyawhsan.notiva.user.service;

import com.kyawhsan.notiva.user.dto.AvatarResponse;
import com.kyawhsan.notiva.user.dto.ChangePasswordRequest;
import com.kyawhsan.notiva.common.response.ImageUploadResponse;
import com.kyawhsan.notiva.user.dto.UpdateProfileRequest;
import com.kyawhsan.notiva.user.dto.UserProfileResponse;
import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.user.enums.UserPlan;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.ai.repository.AiConversationRepository;
import com.kyawhsan.notiva.ai.repository.AiMessageRepository;
import com.kyawhsan.notiva.ai.repository.AiUsageRepository;
import com.kyawhsan.notiva.auth.repository.AuthTokenRepository;
import com.kyawhsan.notiva.note.repository.CategoryRepository;
import com.kyawhsan.notiva.note.repository.NoteRepository;
import com.kyawhsan.notiva.note.repository.NoteVersionRepository;
import com.kyawhsan.notiva.user.repository.UserRepository;
import com.kyawhsan.notiva.common.util.ImageValidator;
import com.kyawhsan.notiva.common.storage.CloudinaryService;
import com.kyawhsan.notiva.security.CurrentUserService;
import com.kyawhsan.notiva.ai.service.NoteEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String AVATAR_FOLDER = "notiva/avatars";

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final CloudinaryService cloudinaryService;
    private final ImageValidator imageValidator;
    private final PasswordEncoder passwordEncoder;

    private final NoteRepository noteRepository;
    private final NoteVersionRepository noteVersionRepository;
    private final CategoryRepository categoryRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AiUsageRepository aiUsageRepository;
    private final AiConversationRepository aiConversationRepository;
    private final AiMessageRepository aiMessageRepository;

    private final NoteEmbeddingService noteEmbeddingService;

    @Value("${app.image.avatar-max-size}")
    private long avatarMaximumSize;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = currentUserService.getCurrentUser();

        return UserProfileResponse.from(currentUser);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(
            UpdateProfileRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        currentUser.setDisplayName(request.displayName().trim());

        User updatedUser = userRepository.save(currentUser);

        return UserProfileResponse.from(updatedUser);
    }

    @Transactional
    public AvatarResponse uploadAvatar(
            MultipartFile file) {
        imageValidator.validate(file, avatarMaximumSize);

        User user = currentUserService.getCurrentUser();

        String previousPublicId = user.getAvatarPublicId();

        ImageUploadResponse uploadedImage = cloudinaryService.uploadImage(file, AVATAR_FOLDER);

        try {
            user.setAvatarUrl(uploadedImage.url());
            user.setAvatarPublicId(uploadedImage.publicId());

            userRepository.save(user);
        } catch (RuntimeException exception) {
            deleteUploadedImageQuietly(uploadedImage.publicId());

            throw exception;
        }

        deletePreviousAvatarQuietly(previousPublicId);

        return new AvatarResponse(user.getAvatarUrl(), user.getAvatarPublicId());
    }

    @Transactional
    public void removeAvatar() {
        User user = currentUserService.getCurrentUser();

        String previousPublicId = user.getAvatarPublicId();

        if (previousPublicId == null || previousPublicId.isBlank()) {
            return;
        }

        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);

        userRepository.save(user);

        deletePreviousAvatarQuietly(previousPublicId);
    }

    private void deletePreviousAvatarQuietly(
            String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinaryService.deleteImage(publicId);
        } catch (RuntimeException exception) {
            log.warn("Unable to delete previous Cloudinary avatar: {}", publicId, exception);
        }
    }

    private void deleteUploadedImageQuietly(
            String publicId) {
        try {
            cloudinaryService.deleteImage(publicId);
        } catch (RuntimeException exception) {
            log.warn("Unable to clean newly uploaded Cloudinary avatar: {}", publicId, exception);
        }
    }

    @Transactional
    public UserProfileResponse upgradePlan() {
        User currentUser = currentUserService.getCurrentUser();

        currentUser.setPlan(UserPlan.PREMIUM);

        User updatedUser = userRepository.save(currentUser);

        return UserProfileResponse.from(updatedUser);
    }

    @Transactional
    public UserProfileResponse downgradePlan() {
        User currentUser = currentUserService.getCurrentUser();

        currentUser.setPlan(UserPlan.NORMAL);

        User updatedUser = userRepository.save(currentUser);

        return UserProfileResponse.from(updatedUser);
    }

    @Transactional
    public void changePassword(
            ChangePasswordRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        String storedPassword = currentUser.getPassword();

        if (storedPassword == null || storedPassword.isBlank()) {
            throw new BadRequestException(
                    "Password change is not available for Google-only accounts");
        }

        if (!passwordEncoder.matches(request.currentPassword(), storedPassword)) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), storedPassword)) {
            throw new BadRequestException(
                    "New password must be different from the current password");
        }

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(currentUser);
    }

    @Transactional
    public void deleteCurrentUserAccount() {
        User currentUser = currentUserService.getCurrentUser();

        String avatarPublicId = currentUser.getAvatarPublicId();

        deleteUserAiData(currentUser);
        noteEmbeddingService.deleteByUserId(currentUser.getId());
        deleteUserNotes(currentUser);

        categoryRepository.deleteAllByUser(currentUser);
        aiUsageRepository.deleteAllByUser(currentUser);
        authTokenRepository.deleteAllByUser(currentUser);

        userRepository.delete(currentUser);

        deletePreviousAvatarQuietly(avatarPublicId);
    }

    private void deleteUserAiData(
            User user) {
        List<AiConversation> conversations = aiConversationRepository.findAllByUser(user);

        for (AiConversation conversation : conversations) {
            aiMessageRepository.deleteAllByConversation(conversation);
        }

        aiConversationRepository.deleteAllByUser(user);
    }

    private void deleteUserNotes(
            User user) {
        List<Note> notes = noteRepository.findAllByUser(user);

        for (Note note : notes) {
            noteVersionRepository.deleteAllByNote(note);
        }

        noteRepository.deleteAllByUser(user);
    }
}
