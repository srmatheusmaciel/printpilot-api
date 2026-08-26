package br.com.printpilot.dto.pricing;

import br.com.printpilot.entity.AreaPricingRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AreaPricingRuleResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal printingCostPerSquareMeter,
        BigDecimal laborCost,
        BigDecimal wastePercentage,
        BigDecimal marginPercentage,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AreaPricingRuleResponse fromEntity(AreaPricingRule rule) {
        return new AreaPricingRuleResponse(
                rule.getId(),
                rule.getProduct().getId(),
                rule.getProduct().getName(),
                rule.getPrintingCostPerSquareMeter(),
                rule.getLaborCost(),
                rule.getWastePercentage(),
                rule.getMarginPercentage(),
                rule.getActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
