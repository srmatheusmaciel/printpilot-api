package br.com.printpilot.service;

import br.com.printpilot.dto.pricing.AreaPricingRuleRequest;
import br.com.printpilot.dto.pricing.AreaPricingRuleResponse;
import br.com.printpilot.entity.AreaPricingRule;
import br.com.printpilot.entity.Product;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.repository.AreaPricingRuleRepository;
import br.com.printpilot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AreaPricingRuleService {

    private final AreaPricingRuleRepository repository;
    private final ProductRepository productRepository;

    @Transactional
    public AreaPricingRuleResponse create(AreaPricingRuleRequest request) {
        Product product = findActiveAreaProduct(request.productId());

        if (repository.existsByProductId(product.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma regra de precificação para o produto: " + product.getName()
            );
        }

        AreaPricingRule rule = AreaPricingRule.builder()
                .product(product)
                .printingCostPerSquareMeter(request.printingCostPerSquareMeter())
                .laborCost(request.laborCost())
                .wastePercentage(request.wastePercentage())
                .marginPercentage(request.marginPercentage())
                .active(request.active() != null ? request.active() : true)
                .build();

        AreaPricingRule saved = repository.save(rule);
        return AreaPricingRuleResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public AreaPricingRuleResponse findByProductId(Long productId) {
        AreaPricingRule rule = findEntityByProductId(productId);
        return AreaPricingRuleResponse.fromEntity(rule);
    }

    @Transactional
    public AreaPricingRuleResponse update(Long id, AreaPricingRuleRequest request) {
        AreaPricingRule rule = findEntityById(id);

        // Valida novo produto se o productId mudou
        if (!rule.getProduct().getId().equals(request.productId())) {
            Product newProduct = findActiveAreaProduct(request.productId());

            if (repository.existsByProductId(newProduct.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Já existe uma regra de precificação para o produto: " + newProduct.getName()
                );
            }

            rule.setProduct(newProduct);
        }

        rule.setPrintingCostPerSquareMeter(request.printingCostPerSquareMeter());
        rule.setLaborCost(request.laborCost());
        rule.setWastePercentage(request.wastePercentage());
        rule.setMarginPercentage(request.marginPercentage());
        if (request.active() != null) {
            rule.setActive(request.active());
        }

        AreaPricingRule updated = repository.save(rule);
        return AreaPricingRuleResponse.fromEntity(updated);
    }

    private Product findActiveAreaProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado: id=" + productId));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Produto inativo: " + product.getName());
        }

        if (product.getPricingType() != PricingType.AREA) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Produto '" + product.getName() + "' não possui PricingType.AREA");
        }

        return product;
    }

    private AreaPricingRule findEntityByProductId(Long productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Regra de precificação não encontrada para o produto id=" + productId));
    }

    private AreaPricingRule findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Regra de precificação não encontrada: id=" + id));
    }
}
