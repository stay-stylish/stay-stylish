package org.example.staystylish.domain.dailyoutfit.repository;

import org.example.staystylish.domain.dailyoutfit.entity.UserItemFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserItemFeedback 엔티티에 대한 데이터 접근을 처리하는 레포지토리 인터페이스
 */
public interface UserItemFeedbackRepository extends JpaRepository<UserItemFeedback, Long> {

    Optional<UserItemFeedback> findByUserIdAndProductId(Long userId, Long productId);

    List<UserItemFeedback> findByUserId(Long userId);
}
