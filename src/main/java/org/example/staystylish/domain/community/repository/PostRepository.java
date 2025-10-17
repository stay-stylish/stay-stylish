package org.example.staystylish.domain.community.repository;

import org.example.staystylish.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
