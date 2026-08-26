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

    @GetMapping
    @Operation(summary = "Listar orçamentos", description = "Lista todos os orçamentos criados.")
    public List<QuoteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar orçamento", description = "Consulta um orçamento persistido pelo ID.")
    public QuoteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
