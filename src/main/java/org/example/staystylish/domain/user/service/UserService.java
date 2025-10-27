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
        User persistentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        persistentUser.updateProfile(nickname, stylePreference, gender);
        return UserResponse.from(persistentUser);
    }

    // 소프트 delete
    @Transactional
    public void softDelete(User user) {
        User persistentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        persistentUser.softDelete();
    }
}
