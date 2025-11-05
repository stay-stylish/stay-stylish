package org.example.staystylish.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.security.UserPrincipal;
import org.example.staystylish.domain.user.code.UserSuccessCode;
import org.example.staystylish.domain.user.dto.request.LoginRequest;
import org.example.staystylish.domain.user.dto.request.ProfileUpdateRequest;
import org.example.staystylish.domain.user.dto.request.RefreshTokenRequest;
import org.example.staystylish.domain.user.dto.request.SignupRequest;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.service.AuthService;
import org.example.staystylish.domain.user.service.EmailVerificationService;
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
    private final EmailVerificationService emailVerificationService;

    /** OAuth 일회용 코드로 토큰 교환 */
    @Operation(summary = "OAuth 토큰 교환", description = "일회용 코드로 실제 토큰을 발급받습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/auth/oauth/exchange")
    public ApiResponse<Map<String, Object>> exchangeOAuthCode(@RequestParam String code) {
        Map<String, Object> tokens = authService.exchangeOAuthCode(code);
        return ApiResponse.of(UserSuccessCode.TOKEN_EXCHANGE_SUCCESS, tokens);
    }

    /** 회원가입 + 이메일 인증 메일 발송 */
    @PostMapping("/auth/signup")
    @Operation(summary = "회원가입", description = "/api/v1/auth/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request,
                                            HttpServletRequest httpRequest) {
        String baseUrl = getBaseUrl(httpRequest);
        UserResponse response = authService.signup(request, baseUrl);
        return ApiResponse.of(UserSuccessCode.SIGNUP_SUCCESS, response);
    }

    /** 이메일 인증 완료 */
    @GetMapping("/auth/verify")
    @Operation(summary = "이메일 인증", description = "메일 링크 클릭 시 호출")
    public ApiResponse<Void> verifyEmail(@RequestParam String token) {
        emailVerificationService.verify(token);
        return ApiResponse.of(UserSuccessCode.EMAIL_VERIFY_SUCCESS, null);
    }

    /** 인증 메일 재발송 */
    @PostMapping("/auth/verify/resend")
    @Operation(summary = "인증 메일 재발송", description = "/api/v1/auth/verify/resend")
    public ApiResponse<Void> resendEmail(@RequestParam String email, HttpServletRequest http) {
        emailVerificationService.resend(email, getBaseUrl(http));
        return ApiResponse.of(UserSuccessCode.EMAIL_VERIFY_RESEND_SUCCESS, null);
    }

    /** 로그인 (Access + Refresh Token 발급) */
    @Operation(summary = "로그인", description = "/api/v1/auth/login")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/auth/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> tokens = authService.login(request);
        return ApiResponse.of(UserSuccessCode.LOGIN_SUCCESS, tokens);
    }

    /** Refresh 토큰으로 Access Token 재발급 */
    @Operation(summary = "Access Token 재발급", description = "/api/v1/auth/refresh")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/auth/refresh")
    public ApiResponse<Map<String, String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, String> newTokens = authService.reissue(request.getRefreshToken());
        return ApiResponse.of(UserSuccessCode.TOKEN_REISSUE_SUCCESS, newTokens);
    }

    /** 내 프로필 조회 */
    @Operation(summary = "내 프로필 조회", description = "/api/v1/users/me",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/users/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUser().getId();
        return ApiResponse.of(UserSuccessCode.PROFILE_FETCH_SUCCESS, userService.getProfile(userId));
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
        Long userId = principal.getUser().getId();
        return ApiResponse.of(UserSuccessCode.PROFILE_UPDATE_SUCCESS,
                userService.updateProfile(userId, request.nickname(), request.stylePreference(), request.toGender()));
    }

    /** 회원 탈퇴 (소프트 삭제) */
    @Operation(summary = "회원 탈퇴", description = "/api/v1/users/me",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @DeleteMapping("/users/me")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal UserPrincipal principal) {
        userService.softDelete(principal.getUser().getId());
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

    /** Base URL 계산 */
    private String getBaseUrl(HttpServletRequest req) {
        String scheme = req.getHeader("X-Forwarded-Proto");
        String host = req.getHeader("X-Forwarded-Host");
        if (scheme != null && host != null) return scheme + "://" + host;
        return req.getScheme() + "://" + req.getServerName() +
                (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort());
    }
}
