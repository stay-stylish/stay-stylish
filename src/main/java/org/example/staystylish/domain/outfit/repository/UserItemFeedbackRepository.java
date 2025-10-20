package org.example.staystylish.domain.outfit.repository;

import org.example.staystylish.domain.outfit.entity.UserItemFeedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * UserItemFeedback 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 */
public interface UserItemFeedbackRepository extends JpaRepository<UserItemFeedback, Long> {

    Optional<UserItemFeedback> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT f FROM UserItemFeedback f JOIN FETCH f.product WHERE f.user.id = :userId ORDER BY f.id DESC")
    List<UserItemFeedback> findRecentFeedbackByUserId(Long userId, Pageable pageable);
}
