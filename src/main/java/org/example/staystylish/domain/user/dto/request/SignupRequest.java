package org.example.staystylish.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.User;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.") String password,
        @NotBlank String nickname,
        String region,
        String stylePreference,
        String gender,
        String provider,
        String providerId
) {

    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .region(region)
                .stylePreference(stylePreference)
                .gender(Gender.valueOf(gender.toUpperCase()))
                .providerId(providerId)
                .build();
    }

    public static SignupRequest of(String email, String password, String nickname,
                                   String region, String stylePreference,
                                   String gender, String provider, String providerId) {
        return new SignupRequest(email, password, nickname, region, stylePreference, gender, provider, providerId);
    }
}
