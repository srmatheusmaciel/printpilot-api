package br.com.printpilot.dto.quote;

import java.math.BigDecimal;

public record QuantityQuoteCalculationResponse(
        Long productId,
        String productName,

        Long materialId,
        String materialName,

        Integer quantity,
        Integer unitsPerSheet,
        Integer requiredSheets,

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
