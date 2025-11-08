package org.example.staystylish.domain.dailyoutfit.repository;

import org.example.staystylish.domain.dailyoutfit.entity.UserCategoryFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCategoryFeedbackRepository extends JpaRepository<UserCategoryFeedback, Long> {

    /**
     * 특정 사용자의 특정 카테고리에 대한 피드백을 조회합니다.
     *
     * @param userId       사용자 ID
     * @param categoryName 카테고리명
     * @return Optional<UserCategoryFeedback>
     */
    Optional<UserCategoryFeedback> findByUserIdAndCategoryName(Long userId, String categoryName);

    /**
     * 특정 사용자의 모든 카테고리 피드백을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return List<UserCategoryFeedback>
     */
    List<UserCategoryFeedback> findByUserId(Long userId);

    /**
     * 특정 사용자의 최신 피드백 N개를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return List<UserCategoryFeedback>
     */
    List<UserCategoryFeedback> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
