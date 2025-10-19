package org.example.staystylish.domain.outfit.repository;

import org.example.staystylish.domain.outfit.model.UserItemFeedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserItemFeedbackRepository extends JpaRepository<UserItemFeedback, Long> {

    Optional<UserItemFeedback> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT f FROM UserItemFeedback f JOIN FETCH f.product WHERE f.user.id = :userId ORDER BY f.id DESC")
    List<UserItemFeedback> findRecentFeedbackByUserId(Long userId, Pageable pageable);
}
