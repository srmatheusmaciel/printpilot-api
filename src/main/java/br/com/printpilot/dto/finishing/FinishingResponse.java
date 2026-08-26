package br.com.printpilot.dto.finishing;

import br.com.printpilot.entity.Finishing;
import br.com.printpilot.enums.FinishingPricingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinishingResponse(
        Long id,
        String name,
        String description,
        FinishingPricingType pricingType,
        BigDecimal cost,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FinishingResponse fromEntity(Finishing finishing) {
        return new FinishingResponse(
                finishing.getId(),
                finishing.getName(),
                finishing.getDescription(),
                finishing.getPricingType(),
                finishing.getCost(),
                finishing.getActive(),
                finishing.getCreatedAt(),
                finishing.getUpdatedAt()
        );
    }
}
