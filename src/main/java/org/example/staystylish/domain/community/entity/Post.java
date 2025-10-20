package org.example.staystylish.domain.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.staystylish.common.entity.BaseEntity;
import org.example.staystylish.domain.user.entity.User;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "posts")
@Where(clause = "deleted_at IS NULL")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int shareCount = 0;

    @Version
    private Long version;

    private LocalDateTime deletedAt;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseLike() { this.likeCount++; }

    public void decreaseLike() { if (this.likeCount > 0) this.likeCount--; }

    public void increaseShare() { this.shareCount++; }

    public void softDelete() { this.deletedAt = LocalDateTime.now(); }
}
