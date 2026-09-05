package com.kyawhsan.notiva.admin.service;

import com.kyawhsan.notiva.admin.dto.AdminDashboardResponse;
import com.kyawhsan.notiva.admin.dto.AdminUserResponse;
import com.kyawhsan.notiva.common.response.PagedResponse;
import com.kyawhsan.notiva.repository.AiUsageRepository;
import com.kyawhsan.notiva.repository.NoteRepository;
import com.kyawhsan.notiva.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final AiUsageRepository aiUsageRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(userRepository.count(), noteRepository.count(),
                aiUsageRepository.sumRequestCount());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> users(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search == null || search.isBlank()) {
            return PagedResponse.from(userRepository.findAll(pageable), AdminUserResponse::from);
        }
        String value = search.trim();
        return PagedResponse.from(userRepository
                .findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(value, value, pageable),
                AdminUserResponse::from);
    }
}
