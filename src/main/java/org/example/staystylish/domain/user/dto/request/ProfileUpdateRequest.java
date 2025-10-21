package org.example.staystylish.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.staystylish.domain.user.entity.Gender;

public record ProfileUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        String stylePreference,

        @Pattern(regexp = "^(?i)MALE|FEMALE$", message = "gender는 MALE 또는 FEMALE만 가능합니다.")
        String gender
) {
    public Gender toGender() {
        return Gender.valueOf(gender.toUpperCase());
    }
}
