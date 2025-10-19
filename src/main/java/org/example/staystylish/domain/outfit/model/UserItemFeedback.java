package org.example.staystylish.domain.outfit.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.staystylish.domain.product.entity.Product;
import org.example.staystylish.domain.user.entity.User;

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

    public void setLikeStatus(LikeStatus likeStatus) {
        this.likeStatus = likeStatus;
    }

    @Builder
    @SuppressWarnings("unused")
    public UserItemFeedback(User user, Product product, LikeStatus likeStatus) {
        this.user = user;
        this.product = product;
        this.likeStatus = likeStatus;
    }
}
