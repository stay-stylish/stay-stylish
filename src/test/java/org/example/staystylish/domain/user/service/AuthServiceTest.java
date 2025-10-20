package org.example.staystylish.domain.user.service;

import org.example.staystylish.common.security.JwtProvider;
import org.example.staystylish.domain.user.dto.request.LoginRequest;
import org.example.staystylish.domain.user.dto.request.SignupRequest;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.Provider;
import org.example.staystylish.domain.user.entity.Role;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private User user;

    @BeforeEach
    void 유저_세팅() {
        MockitoAnnotations.openMocks(this);

        signupRequest = new SignupRequest(
                "test@example.com",
                "password123",
                "수영",
                "서울",
                "스트릿",
                "MALE",
                "LOCAL",
                "1L"
        );

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_pw")
                .nickname("수영")
                .stylePreference("스트릿")
                .gender(Gender.MALE)
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build();
    }

    @Test
    void 회원가입_성공_테스트() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded_pw");
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        UserResponse response = authService.signup(signupRequest);

        // then
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("수영");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 회원가입_실패_중복_이메일() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(UserException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    void 로그인_성공_테스트() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtProvider.generateToken(anyString())).willReturn("mock-jwt-token");

        LoginRequest request = new LoginRequest("test@example.com", "password123");

        // when
        String token = authService.login(request);

        // then
        assertThat(token).isEqualTo("mock-jwt-token");
        verify(jwtProvider, times(1)).generateToken(user.getEmail());
    }

    @Test
    void 로그인_실패_존재하지_않는_사용자() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        LoginRequest request = new LoginRequest("wrong@example.com", "password");

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    void 로그인_실패_비밀번호_불일치() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");
    }
}
