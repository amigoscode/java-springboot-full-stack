package com.amigoscode.product;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        Integer stockLevel
) {
}
