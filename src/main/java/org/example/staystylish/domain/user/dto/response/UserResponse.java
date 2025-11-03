package org.example.staystylish.domain.user.dto.response;

import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.Provider;
import org.example.staystylish.domain.user.entity.Role;
import org.example.staystylish.domain.user.entity.User;

import java.io.Serializable;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String stylePreference,
        Gender gender,
        Role role,
        Provider provider,
        String providerId
) implements Serializable {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getStylePreference(),
                user.getGender(),
                user.getRole(),
                user.getProvider(),
                user.getProviderId()
        );
    }
}