package org.example.staystylish.domain.product.repository;

import org.example.staystylish.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
