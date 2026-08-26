package br.com.printpilot.dto.quote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateQuoteRequest(
        Long customerId,

        @NotNull(message = "ID do produto é obrigatório")
        Long productId,

        @NotNull(message = "ID do material é obrigatório")
        Long materialId,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantity,

        @NotNull(message = "Largura é obrigatória")
        @DecimalMin(value = "0.01", message = "Largura deve ser maior que zero")
        BigDecimal width,

        @NotNull(message = "Altura é obrigatória")
        @DecimalMin(value = "0.01", message = "Altura deve ser maior que zero")
        BigDecimal height,

        @Valid
        List<FinishingCalculationRequest> finishings
) {
}
