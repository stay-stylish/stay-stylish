package org.example.staystylish.domain.productclassification.controller;

import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.domain.productclassification.code.ProductClassificationSuccessCode;
import org.example.staystylish.domain.productclassification.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.productclassification.dto.response.ProductClassificationResponse;
import org.example.staystylish.domain.productclassification.service.ProductClassificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 분류와 관련된 API 요청을 처리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductClassificationController {

    private final ProductClassificationService productClassificationService;

    public ProductClassificationController(ProductClassificationService productClassificationService) {
        this.productClassificationService = productClassificationService;
    }

    @PostMapping("/classify")
    public ApiResponse<ProductClassificationResponse> classifyProduct(@RequestBody ProductClassificationRequest request) {

        ProductClassificationResponse response = productClassificationService.classify(request);

        return ApiResponse.of(ProductClassificationSuccessCode.CLASSIFICATION_SUCCESS, response);
    }
}
