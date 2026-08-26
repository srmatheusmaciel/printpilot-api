package br.com.printpilot.service;

import br.com.printpilot.dto.pricing.QuantityPricingRuleRequest;
import br.com.printpilot.dto.pricing.QuantityPricingRuleResponse;
import br.com.printpilot.entity.Product;
import br.com.printpilot.entity.QuantityPricingRule;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.repository.ProductRepository;
import br.com.printpilot.repository.QuantityPricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QuantityPricingRuleService {

    private final QuantityPricingRuleRepository repository;
    private final ProductRepository productRepository;

    @Transactional
    public QuantityPricingRuleResponse create(QuantityPricingRuleRequest request) {
        if (repository.existsByProductId(request.productId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto já possui uma regra QUANTITY cadastrada");
        }

        Product product = getProductAndValidate(request.productId());

        QuantityPricingRule rule = QuantityPricingRule.builder()
                .product(product)
                .unitsPerSheet(request.unitsPerSheet())
                .printingCostPerUnit(request.printingCostPerUnit())
                .laborCost(request.laborCost())
                .wastePercentage(request.wastePercentage())
                .marginPercentage(request.marginPercentage())
                .active(request.active())
                .build();

        QuantityPricingRule saved = repository.save(rule);
        return QuantityPricingRuleResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public QuantityPricingRuleResponse findByProductId(Long productId) {
        QuantityPricingRule rule = repository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Regra não encontrada para o produto: " + productId));
        return QuantityPricingRuleResponse.fromEntity(rule);
    }

    @Transactional
    public QuantityPricingRuleResponse update(Long id, QuantityPricingRuleRequest request) {
        QuantityPricingRule rule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Regra QUANTITY não encontrada: id=" + id));

        // Se estiver trocando o produto, valida
        if (!rule.getProduct().getId().equals(request.productId())) {
            if (repository.existsByProductId(request.productId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto já possui uma regra QUANTITY cadastrada");
            }
            Product product = getProductAndValidate(request.productId());
            rule.setProduct(product);
        }

        rule.setUnitsPerSheet(request.unitsPerSheet());
        rule.setPrintingCostPerUnit(request.printingCostPerUnit());
        rule.setLaborCost(request.laborCost());
        rule.setWastePercentage(request.wastePercentage());
        rule.setMarginPercentage(request.marginPercentage());
        if (request.active() != null) {
            rule.setActive(request.active());
        }

        QuantityPricingRule updated = repository.save(rule);
        return QuantityPricingRuleResponse.fromEntity(updated);
    }

    private Product getProductAndValidate(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado: id=" + productId));

        if (!product.getActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inativo");
        }

        if (product.getPricingType() != PricingType.QUANTITY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto deve possuir PricingType.QUANTITY");
        }

        return product;
    }
}
