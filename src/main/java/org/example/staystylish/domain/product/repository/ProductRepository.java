package org.example.staystylish.domain.product.repository;

import org.example.staystylish.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Product 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
