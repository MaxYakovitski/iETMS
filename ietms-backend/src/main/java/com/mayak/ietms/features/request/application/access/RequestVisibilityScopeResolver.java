package com.mayak.ietms.features.request.application.access;

import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.infra.persistence.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestVisibilityScopeResolver {

    private final UserQueryService  userQueryService;
    private final ProfileRepository profileRepository;

    public RequestVisibilityScope resolve (Long userId) {
        User user = userQueryService.getEntityById(userId);
        if (user.getUserType() == UserType.ADMIN) {
            return RequestVisibilityScope.all();
        }
        Long departmentId = profileRepository.findDepartmentIdByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User %d has no department profile".formatted(userId)));
        return RequestVisibilityScope.forDepartment(departmentId);
    }
}