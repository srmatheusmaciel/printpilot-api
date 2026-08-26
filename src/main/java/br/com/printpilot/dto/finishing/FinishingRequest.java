package br.com.printpilot.dto.finishing;

import br.com.printpilot.enums.FinishingPricingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FinishingRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String name,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "Tipo de precificação é obrigatório")
        FinishingPricingType pricingType,

        @NotNull(message = "Custo é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "Custo não pode ser negativo")
        BigDecimal cost,

        Boolean active
) {
}
