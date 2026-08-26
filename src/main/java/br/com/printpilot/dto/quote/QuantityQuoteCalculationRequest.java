package br.com.printpilot.dto.quote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record QuantityQuoteCalculationRequest(
        @NotNull(message = "ID do produto é obrigatório")
        Long productId,

        @NotNull(message = "ID do material é obrigatório")
        Long materialId,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantity,

        @Valid
        List<FinishingCalculationRequest> finishings
) {
}
