package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    // 프로필 수정
    @Transactional
    public UserResponse updateProfile(User user, String nickname, String stylePreference, Gender gender) {
        User persistentUser = findUserById(user.getId());
        persistentUser.updateProfile(nickname, stylePreference, gender);
        return UserResponse.from(persistentUser);
    }

    // 소프트 삭제
    @Transactional
    public void softDelete(User user) {
        User persistentUser = findUserById(user.getId());
        persistentUser.softDelete();
    }

    // 공통 유저 조회 헬퍼
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
