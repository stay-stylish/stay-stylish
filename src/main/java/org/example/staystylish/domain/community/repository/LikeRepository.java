package org.example.staystylish.domain.community.repository;

import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.entity.Like;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostAndUser(Post post, User user);
}
