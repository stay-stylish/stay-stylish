package org.example.staystylish.domain.productclassification.repository;

import org.example.staystylish.domain.productclassification.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Product 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 */
public interface ProductClassificationRepository extends JpaRepository<Product, Long> {
}
