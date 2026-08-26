package br.com.printpilot.material;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaterialResponse(
        Long id,
        String name,
        UnitMeasure unitMeasure,
        BigDecimal cost,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MaterialResponse fromEntity(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getName(),
                material.getUnitMeasure(),
                material.getCost(),
                material.getActive(),
                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }
}
