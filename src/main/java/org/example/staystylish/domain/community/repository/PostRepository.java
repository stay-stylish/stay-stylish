package org.example.staystylish.domain.community.repository;

import org.example.staystylish.domain.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 최신순 (기본 정렬)
    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findAllOrderByLatest(Pageable pageable);

    // 좋아요순
    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Post> findAllOrderByLike(Pageable pageable);
}
