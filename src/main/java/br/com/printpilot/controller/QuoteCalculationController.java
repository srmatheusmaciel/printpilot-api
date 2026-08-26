package br.com.printpilot.controller;

import br.com.printpilot.dto.quote.AreaQuoteCalculationRequest;
import br.com.printpilot.dto.quote.QuoteCalculationResponse;
import br.com.printpilot.service.QuoteCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteCalculationController {

    private final QuoteCalculationService service;

    @PostMapping("/calculate")
    public QuoteCalculationResponse calculate(@Valid @RequestBody AreaQuoteCalculationRequest request) {
        return service.calculate(request);
    }
}
