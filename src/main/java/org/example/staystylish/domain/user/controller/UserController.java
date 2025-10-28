package org.example.staystylish.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
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

@Tag(name = "회원 관리", description = "회원가입/로그인 및 내 프로필")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    /** 회원가입 */
    @Operation(summary = "회원가입", description = "/api/v1/auth/signup")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/auth/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.of(UserSuccessCode.SIGNUP_SUCCESS, authService.signup(request));
    }

    /** 로그인 (Access + Refresh Token 발급) */
    @Operation(summary = "로그인", description = "/api/v1/auth/login")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/auth/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> tokens = authService.login(request);
        return ApiResponse.of(UserSuccessCode.LOGIN_SUCCESS, tokens);
    }

    /** Refresh Token으로 Access Token 재발급 */
    @Operation(summary = "Access Token 재발급", description = "/api/v1/auth/refresh")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/auth/refresh")
    public ApiResponse<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> newTokens = authService.reissue(refreshToken);
        return ApiResponse.of(UserSuccessCode.LOGIN_SUCCESS, newTokens);
    }

    /** 내 프로필 조회 */
    @Operation(summary = "내 프로필 조회", description = "/api/v1/users/me",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/users/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.of(UserSuccessCode.PROFILE_FETCH_SUCCESS,
                userService.getProfile(principal.getUser()));
    }

    /** 내 정보 수정 */
    @Operation(summary = "프로필 수정", description = "/api/v1/users/me",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
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

    /** 회원 탈퇴 (소프트 삭제) */
    @Operation(summary = "회원 탈퇴", description = "/api/v1/users/me",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @DeleteMapping("/users/me")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal UserPrincipal principal) {
        userService.softDelete(principal.getUser());
        return ApiResponse.of(UserSuccessCode.USER_DELETE_SUCCESS, null);
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "로그아웃", description = "/api/v1/auth/logout",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getUsername());
        return ApiResponse.of(UserSuccessCode.LOGOUT_SUCCESS, null);
    }
}
