package br.com.printpilot.dto.pricing;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AreaPricingRuleRequest(

        @NotNull(message = "ID do produto é obrigatório")
        Long productId,

        @NotNull(message = "Custo de impressão por m² é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "Custo de impressão não pode ser negativo")
        BigDecimal printingCostPerSquareMeter,

        @NotNull(message = "Mão de obra é obrigatória")
        @DecimalMin(value = "0.0", inclusive = true, message = "Mão de obra não pode ser negativa")
        BigDecimal laborCost,

        @NotNull(message = "Percentual de desperdício é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "Desperdício não pode ser negativo")
        @DecimalMax(value = "100.0", inclusive = true, message = "Desperdício não pode ser maior que 100%")
        BigDecimal wastePercentage,

        @NotNull(message = "Margem é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "Margem deve ser maior que 0%")
        @DecimalMax(value = "100.0", inclusive = false, message = "Margem deve ser menor que 100%")
        BigDecimal marginPercentage,

        Boolean active
) {
}
