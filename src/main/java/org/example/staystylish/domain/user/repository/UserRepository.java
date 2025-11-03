package org.example.staystylish.domain.user.repository;

import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActiveUsers();

    @EntityGraph(attributePaths = {"posts", "likes", "shares"})
    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdWithDetails(@Param("userId") Long userId);

    @Query("""
    SELECT new org.example.staystylish.domain.user.dto.response.UserResponse(
        u.id,
        u.email,
        u.nickname,
        u.stylePreference,
        u.gender,
        u.role,
        u.provider,
        u.providerId
    )
    FROM User u
    WHERE u.id = :userId
    AND u.deletedAt IS NULL
""")
    Optional<UserResponse> findUserSummaryById(@Param("userId") Long userId);
}

