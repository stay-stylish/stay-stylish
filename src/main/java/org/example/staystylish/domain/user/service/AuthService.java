package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.security.JwtProvider;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.dto.request.LoginRequest;
import org.example.staystylish.domain.user.dto.request.SignupRequest;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
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
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }

        User user = request.toEntity(passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    // 로그인
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        return jwtProvider.generateToken(user.getEmail());
    }
}
