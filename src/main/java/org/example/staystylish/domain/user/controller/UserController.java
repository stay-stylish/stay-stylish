package org.example.staystylish.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.user.code.UserSuccessCode;
import org.example.staystylish.domain.user.dto.request.LoginRequest;
import org.example.staystylish.domain.user.dto.request.ProfileUpdateRequest;
import org.example.staystylish.domain.user.dto.request.SignupRequest;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.service.AuthService;
import org.example.staystylish.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    // 회원가입
    @PostMapping("/auth/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.of(UserSuccessCode.SIGNUP_SUCCESS, authService.signup(request));
    }

    // 로그인 (JWT 발급)
    @PostMapping("/auth/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ApiResponse.of(UserSuccessCode.LOGIN_SUCCESS, Map.of("accessToken", token));
    }

    // 내 프로필 조회
    @GetMapping("/users/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(UserSuccessCode.PROFILE_FETCH_SUCCESS,
                userService.getProfile(principal.getUser()));
    }

    // 내 정보 수정
    @PutMapping("/users/me")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ApiResponse.of(UserSuccessCode.PROFILE_UPDATE_SUCCESS,
                userService.updateProfile(
                        principal.getUser(),
                        request.nickname(),
                        request.stylePreference(),
                        request.toGender()
                ));
    }

    // 회원 탈퇴 (소프트 삭제)
    @DeleteMapping("/users/me")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal UserPrincipal principal) {
        userService.softDelete(principal.getUser());
        return ApiResponse.of(UserSuccessCode.USER_DELETE_SUCCESS, null);
    }
}
