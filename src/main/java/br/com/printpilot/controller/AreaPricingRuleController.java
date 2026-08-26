package br.com.printpilot.controller;

import br.com.printpilot.dto.pricing.AreaPricingRuleRequest;
import br.com.printpilot.dto.pricing.AreaPricingRuleResponse;
import br.com.printpilot.service.AreaPricingRuleService;
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

@Tag(name = "Pricing Rules", description = "Gerenciamento de regras de precificação (atualmente suporta PricingType.AREA)")
@RestController
@RequestMapping("/api/pricing-rules/area")
@RequiredArgsConstructor
public class AreaPricingRuleController {

    private final AreaPricingRuleService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar regra de precificação por área", description = "Cria uma nova regra de precificação para um produto AREA.")
    public AreaPricingRuleResponse create(@Valid @RequestBody AreaPricingRuleRequest request) {
        return service.create(request);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Consultar regra por produto", description = "Consulta a regra de precificação pelo ID do produto.")
    public AreaPricingRuleResponse findByProductId(@PathVariable Long productId) {
        return service.findByProductId(productId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar regra", description = "Atualiza uma regra de precificação existente.")
    public AreaPricingRuleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AreaPricingRuleRequest request) {
        return service.update(id, request);
    }
}
