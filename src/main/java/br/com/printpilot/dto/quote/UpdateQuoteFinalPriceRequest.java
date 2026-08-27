package br.com.printpilot.dto.quote;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateQuoteFinalPriceRequest(
        @NotNull(message = "O preço final é obrigatório")
        @DecimalMin(value = "0.01", inclusive = true, message = "O preço final deve ser maior ou igual a 0.01")
        BigDecimal finalPrice
) {
}
