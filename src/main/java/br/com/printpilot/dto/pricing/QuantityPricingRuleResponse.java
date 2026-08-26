package br.com.printpilot.dto.pricing;

import br.com.printpilot.entity.QuantityPricingRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuantityPricingRuleResponse(
        Long id,
        Long productId,
        String productName,
        Integer unitsPerSheet,
        BigDecimal printingCostPerUnit,
        BigDecimal laborCost,
        BigDecimal wastePercentage,
        BigDecimal marginPercentage,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuantityPricingRuleResponse fromEntity(QuantityPricingRule entity) {
        return new QuantityPricingRuleResponse(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getProduct().getName(),
                entity.getUnitsPerSheet(),
                entity.getPrintingCostPerUnit(),
                entity.getLaborCost(),
                entity.getWastePercentage(),
                entity.getMarginPercentage(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
