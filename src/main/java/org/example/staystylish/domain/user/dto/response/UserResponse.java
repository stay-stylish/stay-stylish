package org.example.staystylish.domain.user.dto.response;

import org.example.staystylish.domain.user.entity.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String stylePreference,
        String gender,
        String role,
        String provider,
        String providerId
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStylePreference(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getRole().name(),
                user.getProvider().name(),
                user.getProviderId()
        );
    }
}
