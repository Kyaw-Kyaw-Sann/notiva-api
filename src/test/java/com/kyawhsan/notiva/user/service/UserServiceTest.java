package com.kyawhsan.notiva.user.service;

import com.kyawhsan.notiva.auth.repository.AuthTokenRepository;
import com.kyawhsan.notiva.common.response.ImageUploadResponse;
import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.ai.repository.AiConversationRepository;
import com.kyawhsan.notiva.ai.repository.AiMessageRepository;
import com.kyawhsan.notiva.ai.repository.AiUsageRepository;
import com.kyawhsan.notiva.note.repository.CategoryRepository;
import com.kyawhsan.notiva.note.repository.NoteRepository;
import com.kyawhsan.notiva.note.repository.NoteVersionRepository;
import com.kyawhsan.notiva.user.repository.UserRepository;
import com.kyawhsan.notiva.common.storage.CloudinaryService;
import com.kyawhsan.notiva.security.CurrentUserService;
import com.kyawhsan.notiva.ai.service.NoteEmbeddingService;
import com.kyawhsan.notiva.common.util.ImageValidator;
import com.kyawhsan.notiva.user.dto.ChangePasswordRequest;
import com.kyawhsan.notiva.user.dto.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private ImageValidator imageValidator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NoteRepository noteRepository;
    @Mock private NoteVersionRepository noteVersionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AuthTokenRepository authTokenRepository;
    @Mock private AiUsageRepository aiUsageRepository;
    @Mock private AiConversationRepository aiConversationRepository;
    @Mock private AiMessageRepository aiMessageRepository;
    @Mock private NoteEmbeddingService noteEmbeddingService;

    @InjectMocks private UserService userService;

    @Test
    void updatesOnlyTheAuthenticatedUsersProfile() {
        User user = user(1L);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.updateCurrentUserProfile(new UpdateProfileRequest("  Notiva User  "));

        assertThat(response.displayName()).isEqualTo("Notiva User");
        verify(userRepository).save(user);
    }

    @Test
    void uploadsAnAvatarForTheAuthenticatedUser() {
        User user = user(1L);
        MultipartFile file = mock(MultipartFile.class);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cloudinaryService.uploadImage(file, "notiva/avatars"))
                .thenReturn(new ImageUploadResponse("https://example.test/avatar.jpg", "avatar-id"));
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.uploadAvatar(file);

        assertThat(response.avatarUrl()).isEqualTo("https://example.test/avatar.jpg");
        assertThat(response.avatarPublicId()).isEqualTo("avatar-id");
        verify(imageValidator).validate(file, 0L);
        verify(userRepository).save(user);
    }

    @Test
    void changesPasswordUsingThePasswordEncoder() {
        User user = user(1L);
        user.setPassword("existing-hash");
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("old-password", "existing-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        userService.changePassword(new ChangePasswordRequest("old-password", "new-password"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    void deletesAllUserOwnedDataIncludingEmbeddingChunks() {
        User user = user(1L);
        Note note = new Note();
        note.setId(10L);
        AiConversation conversation = new AiConversation();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(noteRepository.findAllByUser(user)).thenReturn(List.of(note));
        when(aiConversationRepository.findAllByUser(user)).thenReturn(List.of(conversation));

        userService.deleteCurrentUserAccount();

        verify(aiMessageRepository).deleteAllByConversation(conversation);
        verify(aiConversationRepository).deleteAllByUser(user);
        verify(noteEmbeddingService).deleteByUserId(1L);
        verify(noteVersionRepository).deleteAllByNote(note);
        verify(noteRepository).deleteAllByUser(user);
        verify(categoryRepository).deleteAllByUser(user);
        verify(aiUsageRepository).deleteAllByUser(user);
        verify(authTokenRepository).deleteAllByUser(user);
        verify(userRepository).delete(user);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@example.test");
        user.setDisplayName("User");
        return user;
    }
}
