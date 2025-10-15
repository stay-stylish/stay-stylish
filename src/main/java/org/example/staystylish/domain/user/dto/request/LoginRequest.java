package org.example.staystylish.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
    public static LoginRequest of(String email, String password) {
        return new LoginRequest(email, password);
    }
}
