package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.security.JwtProvider;
import org.example.staystylish.domain.user.dto.request.LoginRequest;
import org.example.staystylish.domain.user.dto.request.SignupRequest;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = request.toEntity(passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    // 로그인
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        return jwtProvider.generateToken(user.getEmail());
    }
}
