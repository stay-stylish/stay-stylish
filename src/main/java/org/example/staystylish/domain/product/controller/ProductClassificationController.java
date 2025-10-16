package org.example.staystylish.domain.product.controller;

import org.example.staystylish.domain.product.dto.request.ProductClassificationRequest;
import org.example.staystylish.domain.product.dto.response.ProductClassificationResponse;
import org.example.staystylish.domain.product.service.ProductClassificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductClassificationController {

    private final ProductClassificationService productClassificationService;

    public ProductClassificationController(ProductClassificationService productClassificationService) {
        this.productClassificationService = productClassificationService;
    }

    @PostMapping("/classify")
    public ProductClassificationResponse classifyProduct(@RequestBody ProductClassificationRequest request) {
        return productClassificationService.classify(request);
    }
}
