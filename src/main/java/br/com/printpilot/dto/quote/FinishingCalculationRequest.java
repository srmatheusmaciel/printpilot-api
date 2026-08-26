package br.com.printpilot.dto.quote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FinishingCalculationRequest(

        @NotNull(message = "ID do acabamento é obrigatório")
        Long finishingId,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantity
) {
}
