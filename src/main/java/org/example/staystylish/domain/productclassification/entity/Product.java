package org.example.staystylish.domain.productclassification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.staystylish.domain.productclassification.dto.response.ProductClassificationResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 정보를 나타내는 엔티티 클래스
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

    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_style_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "style_tag")
    private List<String> styleTags = new ArrayList<>();


    // private 생성자로 직접 new 생성 방지
    private Product(String name) {
        this.name = name;
    }

    // 정적 팩토리 메소드
    // 새로운 Product 엔티티를 생성합니다.
    public static Product create(String name) {
        // 유효성 검증, 초기 상태 설정, 기본값 부여는 정적 팩토리 내부에서 수행
        // 현재는 name만 있으므로 간단하게 처리
        return new Product(name);
    }

    /**
     * 상품 분류 결과를 엔티티에 업데이트하는 메서드
     * @param response 상품 분류 API로부터 받은 응답 DTO
     */
    public void updateClassification(ProductClassificationResponse response) {
        this.category = response.category();
        this.subCategory = response.subCategory();
        this.styleTags = new ArrayList<>(response.styleTags()); // 방어적 복사
    }
}
