package org.example.staystylish.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.security.oauth.OAuth2UserInfo;
import org.example.staystylish.common.security.oauth.OAuth2UserInfoFactory;
import org.example.staystylish.domain.user.entity.Provider;
import org.example.staystylish.domain.user.entity.Role;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어떤 OAuth 제공자인지 (google, kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

        // provider별로 공통 UserInfo 변환
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        log.info("OAuth2 로그인 요청 - provider={}, email={}, name={}",
                registrationId, userInfo.getEmail(), userInfo.getName());

        // 이메일 기준으로 기존 사용자 확인
        Optional<User> optionalUser = userRepository.findByEmail(userInfo.getEmail());
        boolean isNewUser = optionalUser.isEmpty(); // 신규 유저 여부 판단

        // 신규 유저면 기본정보로 가입
        User user = optionalUser.orElseGet(() -> {
            log.info("신규 {} 유저 회원가입: {}", registrationId, userInfo.getEmail());
            return userRepository.save(User.builder()
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getName())
                    .provider(Provider.valueOf(registrationId.toUpperCase()))
                    .providerId(userInfo.getId())
                    .password("social-user-placeholder")
                    .role(Role.USER)
                    .build());
        });

        log.info("OAuth2 로그인 성공 - 사용자: {} (신규 여부: {})", user.getEmail(), isNewUser);

        // UserPrincipal에 신규 유저 여부 추가
        return new UserPrincipal(user, oAuth2User.getAttributes(), isNewUser);
    }
}
