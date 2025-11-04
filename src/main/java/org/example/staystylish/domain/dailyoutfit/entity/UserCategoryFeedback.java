package org.example.staystylish.domain.dailyoutfit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.staystylish.common.entity.BaseEntity;
import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;
import org.example.staystylish.domain.user.entity.User;

/**
 * 사용자의 카테고리 피드백 정보를 나타내는 엔티티 클래스
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_category_feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_category_unique",
                        columnNames = {"user_id", "categoryName"}
                )
        }
)
public class UserCategoryFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_category_feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String categoryName;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeStatus likeStatus;

    @Builder
    private UserCategoryFeedback(User user, String categoryName, LikeStatus likeStatus) {
        this.user = user;
        this.categoryName = categoryName;
        this.likeStatus = likeStatus;
    }

    /**
     * 새로운 UserCategoryFeedback 엔티티를 생성합니다.
     *
     * @param user         피드백을 남긴 사용자
     * @param categoryName 피드백 대상 카테고리명
     * @param likeStatus   좋아요/싫어요 상태
     * @return 생성된 UserCategoryFeedback 엔티티
     */
    public static UserCategoryFeedback create(User user, String categoryName, LikeStatus likeStatus) {
        return new UserCategoryFeedback(user, categoryName, likeStatus);
    }
}
