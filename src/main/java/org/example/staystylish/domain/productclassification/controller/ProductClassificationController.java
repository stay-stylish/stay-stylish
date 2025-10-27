package org.example.staystylish.domain.productclassification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 상품 분류와 관련된 API 요청을 처리하는 컨트롤러 클래스
 */
@Tag(name = "상품 분류 API", description = "상품 분류 관련된 API 요청 처리")
@RestController
@RequestMapping("/api/products")
public class ProductClassificationController {

    private final ProductClassificationService productClassificationService;

    public ProductClassificationController(ProductClassificationService productClassificationService) {
        this.productClassificationService = productClassificationService;
    }

    @Operation(summary = "상품 분류", description = "상품 분류 요청을 처리")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/classify")
    // 상품 분류 요청을 처리하고 결과를 반환합니다.
    public ApiResponse<ProductClassificationResponse> classifyProduct(
            @RequestBody ProductClassificationRequest request) {

        ProductClassificationResponse response = productClassificationService.classify(request);

        return ApiResponse.of(ProductClassificationSuccessCode.CLASSIFICATION_SUCCESS, response);
    }
}
