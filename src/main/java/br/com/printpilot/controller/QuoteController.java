package br.com.printpilot.controller;

import br.com.printpilot.dto.quote.CreateQuoteRequest;
import br.com.printpilot.dto.quote.QuoteResponse;
import br.com.printpilot.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Quotes", description = "Gerenciamento e persistência de orçamentos")
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar e persistir orçamento", description = "Recalcula os valores no backend usando o Pricing Engine e persiste o orçamento como DRAFT. customerId é opcional.")
    public QuoteResponse create(@Valid @RequestBody CreateQuoteRequest request) {
        return service.create(request);
    }

    @PostMapping("/quantity")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar e persistir orçamento QUANTITY", description = "Calcula novamente e persiste um orçamento para produtos com PricingType.QUANTITY.")
    public QuoteResponse createQuantity(@Valid @RequestBody br.com.printpilot.dto.quote.CreateQuantityQuoteRequest request) {
        return service.createQuantity(request);
    }

    @GetMapping
    @Operation(summary = "Listar orçamentos", description = "Lista todos os orçamentos criados.")
    public List<QuoteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID", description = "Retorna os detalhes completos de um orçamento específico.")
    public QuoteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do orçamento", description = "Altera o status comercial de um orçamento seguindo as transições permitidas (ex: DRAFT -> SENT -> APPROVED).")
    public QuoteResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody br.com.printpilot.dto.quote.UpdateQuoteStatusRequest request) {
        return service.updateStatus(id, request);
    }

    @PatchMapping("/{id}/final-price")
    @Operation(summary = "Atualizar preço final negociado", description = "Permite alterar o preço comercial final do orçamento, desde que não esteja concluído/rejeitado/expirado.")
    public QuoteResponse updateFinalPrice(
            @PathVariable Long id,
            @Valid @RequestBody br.com.printpilot.dto.quote.UpdateQuoteFinalPriceRequest request) {
        return service.updateFinalPrice(id, request);
    }
}
