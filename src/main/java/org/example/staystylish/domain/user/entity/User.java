package org.example.staystylish.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.staystylish.common.entity.BaseEntity;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 30)
    private String stylePreference;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    private LocalDateTime deletedAt;

    public void updateProfile(String nickname, String stylePreference, Gender gender) {
        this.nickname = nickname;
        this.stylePreference = stylePreference;
        this.gender = gender;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
