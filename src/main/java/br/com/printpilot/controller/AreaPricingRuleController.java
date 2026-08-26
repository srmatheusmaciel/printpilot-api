package br.com.printpilot.controller;

import br.com.printpilot.dto.pricing.AreaPricingRuleRequest;
import br.com.printpilot.dto.pricing.AreaPricingRuleResponse;
import br.com.printpilot.service.AreaPricingRuleService;
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

@RestController
@RequestMapping("/api/pricing-rules/area")
@RequiredArgsConstructor
public class AreaPricingRuleController {

    private final AreaPricingRuleService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AreaPricingRuleResponse create(@Valid @RequestBody AreaPricingRuleRequest request) {
        return service.create(request);
    }

    @GetMapping("/product/{productId}")
    public AreaPricingRuleResponse findByProductId(@PathVariable Long productId) {
        return service.findByProductId(productId);
    }

    @PutMapping("/{id}")
    public AreaPricingRuleResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AreaPricingRuleRequest request) {
        return service.update(id, request);
    }
}
