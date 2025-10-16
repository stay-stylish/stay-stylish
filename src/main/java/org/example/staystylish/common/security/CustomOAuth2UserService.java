package org.example.staystylish.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.domain.user.entity.Provider;
import org.example.staystylish.domain.user.entity.Role;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // Google OAuth2 로그인 처리
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어떤 OAuth provider인지 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equals(registrationId)) {
            throw new OAuth2AuthenticationException("지원하지 않는 로그인 제공자입니다: " + registrationId);
        }

        // 구글 사용자 정보 파싱
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        log.info("Google OAuth2 로그인 요청: email={}, name={}", email, name);

        // 기존 회원 조회 or 신규 회원 등록
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("새로운 구글 유저 회원가입: {}", email);
                    User newUser = User.builder()
                            .email(email)
                            .nickname(name)
                            .provider(Provider.GOOGLE)
                            .providerId(providerId)
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        log.info("Google OAuth2 로그인 성공 - 사용자: {}", user.getEmail());

        // UserPrincipal 리턴 (JWT, SecurityContext에서 공통 사용)
        return new UserPrincipal(user, attributes);
    }
}
