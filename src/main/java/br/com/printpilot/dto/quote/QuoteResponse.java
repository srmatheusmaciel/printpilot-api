package br.com.printpilot.dto.quote;

import br.com.printpilot.entity.Quote;
import br.com.printpilot.enums.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuoteResponse(
        Long id,

        Long productId,
        String productName,

        Long materialId,
        String materialName,

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

        BigDecimal suggestedPrice,
        BigDecimal finalPrice,

        QuoteStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuoteResponse fromEntity(Quote quote) {
        return new QuoteResponse(
                quote.getId(),

                quote.getProduct().getId(),
                quote.getProductName(),

                quote.getMaterial().getId(),
                quote.getMaterialName(),

                quote.getQuantity(),

                quote.getWidth(),
                quote.getHeight(),

                quote.getUnitArea(),
                quote.getTotalArea(),

                quote.getMaterialCost(),
                quote.getPrintingCost(),
                quote.getFinishingCost(),
                quote.getWasteCost(),
                quote.getLaborCost(),

                quote.getTotalCost(),

                quote.getMarginPercentage(),

                quote.getSuggestedPrice(),
                quote.getFinalPrice(),

                quote.getStatus(),

                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}
