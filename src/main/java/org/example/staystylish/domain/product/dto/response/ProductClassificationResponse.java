package org.example.staystylish.domain.product.dto.response;

import java.util.List;

public record ProductClassificationResponse(
        String category,
        String sub_category,
        List<String> style_tags
) {
}