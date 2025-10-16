package org.example.staystylish.domain.product.controller;

import org.example.staystylish.domain.product.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.product.dto.response.ProductClassificationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductClassificationController {

    // 나중에 Service를 주입받을 필드. Spring AI 의존성 설정 후 해제 예정
    // private final ProductClassificationService productClassificationService;

    // 임시 하드코딩된 응답
    @PostMapping("/classify")
    public ProductClassificationResponse classifyProduct(@RequestBody ProductClassificationRequest request) {

        // return productClassificationService.classify(request);
        return new ProductClassificationResponse("상의", "맨투맨", java.util.List.of("캐주얼"));
    }
}
