package br.com.printpilot.service;

import br.com.printpilot.dto.quote.AreaQuoteCalculationRequest;
import br.com.printpilot.dto.quote.FinishingCalculationRequest;
import br.com.printpilot.dto.quote.QuoteCalculationResponse;
import br.com.printpilot.dto.quote.QuantityQuoteCalculationRequest;
import br.com.printpilot.dto.quote.QuantityQuoteCalculationResponse;
import br.com.printpilot.entity.AreaPricingRule;
import br.com.printpilot.entity.Finishing;
import br.com.printpilot.entity.Material;
import br.com.printpilot.entity.Product;
import br.com.printpilot.entity.QuantityPricingRule;
import br.com.printpilot.enums.FinishingPricingType;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.enums.UnitMeasure;
import br.com.printpilot.repository.AreaPricingRuleRepository;
import br.com.printpilot.repository.FinishingRepository;
import br.com.printpilot.repository.MaterialRepository;
import br.com.printpilot.repository.ProductRepository;
import br.com.printpilot.repository.QuantityPricingRuleRepository;
import br.com.printpilot.repository.MaterialRepository;
import br.com.printpilot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteCalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final FinishingRepository finishingRepository;
    private final AreaPricingRuleRepository areaPricingRuleRepository;
    private final QuantityPricingRuleRepository quantityPricingRuleRepository;

    @Transactional(readOnly = true)
    public QuoteCalculationResponse calculate(AreaQuoteCalculationRequest request) {

        // 1. Buscar e validar produto
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado: id=" + request.productId()));

        // 2. Validar produto ativo
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Produto inativo: " + product.getName());
        }

        // 3. Validar PricingType.AREA
        if (product.getPricingType() != PricingType.AREA) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Produto '" + product.getName() + "' não suporta precificação por área (PricingType.AREA)");
        }

        // 4. Buscar e validar material
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Material não encontrado: id=" + request.materialId()));

        // 5. Validar material ativo
        if (!Boolean.TRUE.equals(material.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Material inativo: " + material.getName());
        }

        // 6. Validar UnitMeasure.SQUARE_METER
        if (material.getUnitMeasure() != UnitMeasure.SQUARE_METER) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Material '" + material.getName() + "' não é medido em m² (UnitMeasure.SQUARE_METER)");
        }

        // 7. Buscar regra de precificação
        AreaPricingRule rule = areaPricingRuleRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Regra de precificação não encontrada para o produto: " + product.getName()));

        // 8. Validar regra ativa
        if (!Boolean.TRUE.equals(rule.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Regra de precificação inativa para o produto: " + product.getName());
        }

        // 9. Calcular área
        BigDecimal quantity = BigDecimal.valueOf(request.quantity());
        BigDecimal unitArea = request.width().multiply(request.height()).setScale(SCALE, ROUNDING);
        BigDecimal totalArea = unitArea.multiply(quantity).setScale(SCALE, ROUNDING);

        // 10. Calcular material
        BigDecimal materialCost = totalArea.multiply(material.getCost()).setScale(SCALE, ROUNDING);

        // 11. Calcular impressão
        BigDecimal printingCost = totalArea
                .multiply(rule.getPrintingCostPerSquareMeter())
                .setScale(SCALE, ROUNDING);

        // 12. Calcular acabamentos
        BigDecimal finishingCost = calculateFinishingCost(request.finishings(), totalArea);

        // 13. Calcular desperdício: (materialCost + printingCost) * (wastePercentage / 100)
        BigDecimal wasteCost = materialCost.add(printingCost)
                .multiply(rule.getWastePercentage())
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING);

        // 14. Mão de obra
        BigDecimal laborCost = rule.getLaborCost().setScale(SCALE, ROUNDING);

        // 15. Custo total
        BigDecimal totalCost = materialCost
                .add(printingCost)
                .add(finishingCost)
                .add(wasteCost)
                .add(laborCost)
                .setScale(SCALE, ROUNDING);

        // 16. Preço sugerido: totalCost / (1 - marginPercentage / 100)
        BigDecimal marginFactor = BigDecimal.ONE
                .subtract(rule.getMarginPercentage().divide(BigDecimal.valueOf(100), 10, ROUNDING));
        BigDecimal suggestedPrice = totalCost.divide(marginFactor, SCALE, ROUNDING);

        return new QuoteCalculationResponse(
                product.getId(),
                product.getName(),
                request.quantity(),
                request.width().setScale(SCALE, ROUNDING),
                request.height().setScale(SCALE, ROUNDING),
                unitArea,
                totalArea,
                materialCost,
                printingCost,
                finishingCost,
                wasteCost,
                laborCost,
                totalCost,
                rule.getMarginPercentage().setScale(SCALE, ROUNDING),
                suggestedPrice
        );
    }

    @Transactional(readOnly = true)
    public QuantityQuoteCalculationResponse calculateQuantity(QuantityQuoteCalculationRequest request) {

        // 1. Buscar e validar produto
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado: id=" + request.productId()));

        // 2. Validar produto ativo
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Produto inativo: " + product.getName());
        }

        // 3. Validar PricingType.QUANTITY
        if (product.getPricingType() != PricingType.QUANTITY) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Produto '" + product.getName() + "' não suporta precificação por quantidade (PricingType.QUANTITY)");
        }

        // 4. Buscar e validar material
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Material não encontrado: id=" + request.materialId()));

        // 5. Validar material ativo
        if (!Boolean.TRUE.equals(material.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Material inativo: " + material.getName());
        }

        // 6. Validar UnitMeasure.SHEET
        if (material.getUnitMeasure() != UnitMeasure.SHEET) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Material '" + material.getName() + "' não é medido em folha (UnitMeasure.SHEET)");
        }

        // 7. Buscar regra de precificação
        QuantityPricingRule rule = quantityPricingRuleRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Regra de precificação não encontrada para o produto: " + product.getName()));

        // 8. Validar regra ativa
        if (!Boolean.TRUE.equals(rule.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Regra de precificação inativa para o produto: " + product.getName());
        }

        // 9. Calcular quantidade de folhas (arredondar para cima)
        BigDecimal quantity = BigDecimal.valueOf(request.quantity());
        BigDecimal unitsPerSheet = BigDecimal.valueOf(rule.getUnitsPerSheet());
        BigDecimal requiredSheetsBd = quantity.divide(unitsPerSheet, 0, RoundingMode.UP);
        Integer requiredSheets = requiredSheetsBd.intValue();

        // 10. Calcular material
        BigDecimal materialCost = requiredSheetsBd.multiply(material.getCost()).setScale(SCALE, ROUNDING);

        // 11. Calcular impressão
        BigDecimal printingCost = quantity
                .multiply(rule.getPrintingCostPerUnit())
                .setScale(SCALE, ROUNDING);

        // 12. Calcular acabamentos (rejeitando AREA)
        BigDecimal finishingCost = calculateFinishingCostQuantity(request.finishings());

        // 13. Calcular desperdício
        BigDecimal wasteCost = materialCost.add(printingCost)
                .multiply(rule.getWastePercentage())
                .divide(BigDecimal.valueOf(100), SCALE, ROUNDING);

        // 14. Mão de obra
        BigDecimal laborCost = rule.getLaborCost().setScale(SCALE, ROUNDING);

        // 15. Custo total
        BigDecimal totalCost = materialCost
                .add(printingCost)
                .add(finishingCost)
                .add(wasteCost)
                .add(laborCost)
                .setScale(SCALE, ROUNDING);

        // 16. Preço sugerido
        BigDecimal marginFactor = BigDecimal.ONE
                .subtract(rule.getMarginPercentage().divide(BigDecimal.valueOf(100), 10, ROUNDING));
        BigDecimal suggestedPrice = totalCost.divide(marginFactor, SCALE, ROUNDING);

        return new QuantityQuoteCalculationResponse(
                product.getId(),
                product.getName(),
                material.getId(),
                material.getName(),
                request.quantity(),
                rule.getUnitsPerSheet(),
                requiredSheets,
                materialCost,
                printingCost,
                finishingCost,
                wasteCost,
                laborCost,
                totalCost,
                rule.getMarginPercentage().setScale(SCALE, ROUNDING),
                suggestedPrice
        );
    }

    private BigDecimal calculateFinishingCost(
            List<FinishingCalculationRequest> finishings,
            BigDecimal totalArea) {

        if (finishings == null || finishings.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (FinishingCalculationRequest item : finishings) {
            Finishing finishing = finishingRepository.findById(item.finishingId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Acabamento não encontrado: id=" + item.finishingId()));

            if (!Boolean.TRUE.equals(finishing.getActive())) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Acabamento inativo: " + finishing.getName());
            }

            BigDecimal cost = switch (finishing.getPricingType()) {
                case FinishingPricingType.UNIT ->
                        finishing.getCost()
                                .multiply(BigDecimal.valueOf(item.quantity()))
                                .setScale(SCALE, ROUNDING);

                case FinishingPricingType.AREA ->
                        finishing.getCost()
                                .multiply(totalArea)
                                .setScale(SCALE, ROUNDING);

                case FinishingPricingType.FIXED ->
                        finishing.getCost().setScale(SCALE, ROUNDING);
            };

            total = total.add(cost);
        }

        return total.setScale(SCALE, ROUNDING);
    }

    private BigDecimal calculateFinishingCostQuantity(
            List<FinishingCalculationRequest> finishings) {

        if (finishings == null || finishings.isEmpty()) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (FinishingCalculationRequest item : finishings) {
            Finishing finishing = finishingRepository.findById(item.finishingId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Acabamento não encontrado: id=" + item.finishingId()));

            if (!Boolean.TRUE.equals(finishing.getActive())) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Acabamento inativo: " + finishing.getName());
            }

            if (finishing.getPricingType() == FinishingPricingType.AREA) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Acabamento '" + finishing.getName() + "' utiliza precificação por AREA, o que não é suportado em orçamentos QUANTITY");
            }

            BigDecimal cost = switch (finishing.getPricingType()) {
                case FinishingPricingType.UNIT ->
                        finishing.getCost()
                                .multiply(BigDecimal.valueOf(item.quantity()))
                                .setScale(SCALE, ROUNDING);

                case FinishingPricingType.FIXED ->
                        finishing.getCost().setScale(SCALE, ROUNDING);

                default -> BigDecimal.ZERO;
            };

            total = total.add(cost);
        }

        return total.setScale(SCALE, ROUNDING);
    }
}
