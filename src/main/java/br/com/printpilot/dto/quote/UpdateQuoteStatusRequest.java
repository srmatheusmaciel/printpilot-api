package br.com.printpilot.dto.quote;

import br.com.printpilot.enums.QuoteStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateQuoteStatusRequest(
        @NotNull(message = "O novo status é obrigatório")
        QuoteStatus status
) {
}
