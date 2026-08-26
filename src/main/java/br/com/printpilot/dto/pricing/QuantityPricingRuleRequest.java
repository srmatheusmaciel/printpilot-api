package br.com.printpilot.dto.pricing;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record QuantityPricingRuleRequest(
        @NotNull(message = "O ID do produto é obrigatório")
        Long productId,

        @NotNull(message = "Unidades por folha é obrigatório")
        @Positive(message = "Unidades por folha deve ser maior que zero")
        Integer unitsPerSheet,

        @NotNull(message = "Custo de impressão por unidade é obrigatório")
        @DecimalMin(value = "0.0", message = "Custo de impressão não pode ser negativo")
        BigDecimal printingCostPerUnit,

        @NotNull(message = "Custo de mão de obra é obrigatório")
        @DecimalMin(value = "0.0", message = "Mão de obra não pode ser negativa")
        BigDecimal laborCost,

        @NotNull(message = "Desperdício é obrigatório")
        @DecimalMin(value = "0.0", message = "Desperdício mínimo é 0%")
        @DecimalMax(value = "100.0", message = "Desperdício máximo é 100%")
        BigDecimal wastePercentage,

        @NotNull(message = "Margem é obrigatória")
        @DecimalMin(value = "0.01", message = "Margem mínima deve ser maior que 0%")
        @DecimalMax(value = "99.99", message = "Margem máxima deve ser menor que 100%")
        BigDecimal marginPercentage,

        Boolean active
) {}
