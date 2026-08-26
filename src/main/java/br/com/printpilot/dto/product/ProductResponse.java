package br.com.printpilot.dto.product;

import br.com.printpilot.entity.Product;
import br.com.printpilot.enums.PricingType;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        PricingType pricingType,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPricingType(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
