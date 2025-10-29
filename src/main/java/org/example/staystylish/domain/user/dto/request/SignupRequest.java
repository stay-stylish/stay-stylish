package org.example.staystylish.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.Provider;
import org.example.staystylish.domain.user.entity.User;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.") String password,
        @NotBlank String nickname,
        String stylePreference,
        @Pattern(regexp = "^(?i)MALE|FEMALE$", message = "gender는 MALE 또는 FEMALE만 가능합니다.")
        String gender,
        String provider,
        String providerId
) {

    public User toEntity(String encodedPassword) {
        Provider resolvedProvider = (provider == null || provider.isBlank())
                ? Provider.LOCAL
                : Provider.valueOf(provider.toUpperCase());

        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .stylePreference(stylePreference)
                .gender(Gender.valueOf(gender.toUpperCase()))
                .providerId(providerId)
                .build();
    }

    public static SignupRequest of(String email, String password, String nickname,
                                   String stylePreference,
                                   String gender, String provider, String providerId) {
        return new SignupRequest(email, password, nickname, stylePreference, gender, provider, providerId);
    }
}
