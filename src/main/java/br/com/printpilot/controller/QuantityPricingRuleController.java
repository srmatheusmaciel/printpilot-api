package br.com.printpilot.controller;

import br.com.printpilot.dto.pricing.QuantityPricingRuleRequest;
import br.com.printpilot.dto.pricing.QuantityPricingRuleResponse;
import br.com.printpilot.service.QuantityPricingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Quantity Pricing Rules", description = "Gerenciamento de regras de precificação por quantidade")
@RestController
@RequestMapping("/api/pricing-rules/quantity")
@RequiredArgsConstructor
public class QuantityPricingRuleController {

    private final QuantityPricingRuleService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar regra QUANTITY", description = "Cria uma nova regra de precificação para um produto do tipo QUANTITY.")
    public QuantityPricingRuleResponse create(@Valid @RequestBody QuantityPricingRuleRequest request) {
        return service.create(request);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Consultar regra por produto", description = "Retorna a regra de precificação configurada para o ID do produto informado.")
    public QuantityPricingRuleResponse findByProductId(@PathVariable Long productId) {
        return service.findByProductId(productId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar regra QUANTITY", description = "Atualiza os valores de uma regra QUANTITY existente.")
    public QuantityPricingRuleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody QuantityPricingRuleRequest request) {
        return service.update(id, request);
    }
}
