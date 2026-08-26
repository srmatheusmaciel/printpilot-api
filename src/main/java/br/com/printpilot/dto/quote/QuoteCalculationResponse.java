package br.com.printpilot.dto.quote;

import java.math.BigDecimal;

public record QuoteCalculationResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal width,
        BigDecimal height,
        BigDecimal unitArea,
        BigDecimal totalArea,
        BigDecimal materialCost,
        BigDecimal printingCost,
        BigDecimal finishingCost,
        BigDecimal wasteCost,
        BigDecimal laborCost,
        BigDecimal totalCost,
        BigDecimal marginPercentage,
        BigDecimal suggestedPrice
) {
}
