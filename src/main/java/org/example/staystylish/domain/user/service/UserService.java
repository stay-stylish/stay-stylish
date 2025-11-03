package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "userProfile", key = "#user.id")
    public UserResponse getProfile(User user) {
        return userRepository.findUserSummaryById(user.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    // 프로필 수정
    @Transactional
    @CacheEvict(value = "userProfile", key = "#user.id") // 수정 시 캐시 무효화
    public UserResponse updateProfile(User user, String nickname, String stylePreference, Gender gender) {
        User persistentUser = findUserById(user.getId());
        persistentUser.updateProfile(nickname, stylePreference, gender);
        return UserResponse.from(persistentUser);
    }

    // 소프트 삭제
    @Transactional
    @CacheEvict(value = "userProfile", key = "#user.id") // 탈퇴 시 캐시 무효화
    public void softDelete(User user) {
        User persistentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        persistentUser.softDelete();
    }

    // 공통 유저 조회 헬퍼
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
