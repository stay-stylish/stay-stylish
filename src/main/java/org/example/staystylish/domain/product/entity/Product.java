package org.example.staystylish.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 정보를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name;

    // private 생성자로 직접 new 생성 방지
    private Product(String name) {
        this.name = name;
    }

    // 정적 팩토리 메소드
    public static Product create(String name) {
        // 유효성 검증, 초기 상태 설정, 기본값 부여는 정적 팩토리 내부에서 수행
        // 현재는 name만 있으므로 간단하게 처리
        return new Product(name);
    }}
