package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "userProfile", key = "#userId", unless = "#result == null")
    public UserResponse getProfile(Long userId) {
        log.info("DB에서 사용자 프로필 조회: {}", userId);
        return userRepository.findUserSummaryById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));
    }

    // 프로필 수정
    @Transactional
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserResponse updateProfile(Long userId, String nickname, String stylePreference, Gender gender) {
        User persistentUser = findUserById(userId);
        persistentUser.updateProfile(nickname, stylePreference, gender);

        log.info("프로필 수정 완료: {}", persistentUser.getEmail());
        return UserResponse.from(persistentUser);
    }

    // 소프트 삭제
    @Transactional
    @CacheEvict(value = "userProfile", key = "#userId")
    public void softDelete(Long userId) {
        User persistentUser = findUserById(userId);
        persistentUser.softDelete();
        log.info("사용자 탈퇴 처리 완료: {}", persistentUser.getEmail());
    }

    // 공통 유저 조회 헬퍼
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(UserErrorCode.USER_NOT_FOUND));
    }
}
