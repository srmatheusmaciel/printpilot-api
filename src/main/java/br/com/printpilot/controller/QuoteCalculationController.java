package br.com.printpilot.controller;

import br.com.printpilot.dto.quote.AreaQuoteCalculationRequest;
import br.com.printpilot.dto.quote.QuoteCalculationResponse;
import br.com.printpilot.service.QuoteCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Quote Calculation", description = "Motor de simulação de orçamento")
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteCalculationController {

    private final QuoteCalculationService service;

    @PostMapping("/calculate")
    @Operation(summary = "Calcular orçamento", description = "Calcula um orçamento utilizando produto, material, dimensões, quantidade, acabamentos e regra de precificação. O resultado NÃO é persistido.")
    public QuoteCalculationResponse calculate(@Valid @RequestBody AreaQuoteCalculationRequest request) {
        return service.calculate(request);
    }
}
