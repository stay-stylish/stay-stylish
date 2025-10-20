package org.example.staystylish.domain.outfit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.staystylish.domain.outfit.enums.LikeStatus;
import org.example.staystylish.domain.product.entity.Product;
import org.example.staystylish.domain.user.entity.User;

/**
 * 사용자의 아이템 피드백 정보를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_item_feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "user_item_unique",
                        columnNames = {"user_id", "product_id"}
                )
        }
)
public class UserItemFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_item_feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeStatus likeStatus;

    @Builder
    @SuppressWarnings("unused")
    // private 생성자로 직접 new 생성 방지
    private UserItemFeedback(User user, Product product, LikeStatus likeStatus) {
        this.user = user;
        this.product = product;
        this.likeStatus = likeStatus;
    }

    // 정적 팩토리 메소드
    public static UserItemFeedback create(User user, Product product, LikeStatus likeStatus) {
        return new UserItemFeedback(user, product, likeStatus);
    }
    public void setLikeStatus(LikeStatus likeStatus) {
        this.likeStatus = likeStatus;
    }
}
