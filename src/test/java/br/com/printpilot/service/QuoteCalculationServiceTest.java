package br.com.printpilot.service;

import br.com.printpilot.dto.quote.AreaQuoteCalculationRequest;
import br.com.printpilot.dto.quote.FinishingCalculationRequest;
import br.com.printpilot.dto.quote.QuantityQuoteCalculationRequest;
import br.com.printpilot.dto.quote.QuantityQuoteCalculationResponse;
import br.com.printpilot.dto.quote.QuoteCalculationResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteCalculationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private FinishingRepository finishingRepository;

    @Mock
    private AreaPricingRuleRepository areaPricingRuleRepository;

    @Mock
    private QuantityPricingRuleRepository quantityPricingRuleRepository;

    @InjectMocks
    private QuoteCalculationService quoteCalculationService;

    // --- AREA PRICING TESTS ---

    @Test
    @DisplayName("Should calculate AREA quote correctly with finishings")
    void shouldCalculateAreaQuoteCorrectly() {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("Banner");
        product.setPricingType(PricingType.AREA);
        product.setActive(true);

        Material material = new Material();
        material.setId(1L);
        material.setName("Lona 440g");
        material.setUnitMeasure(UnitMeasure.SQUARE_METER);
        material.setCost(new BigDecimal("18.50"));
        material.setActive(true);

        AreaPricingRule rule = new AreaPricingRule();
        rule.setActive(true);
        rule.setPrintingCostPerSquareMeter(new BigDecimal("10.00"));
        rule.setLaborCost(new BigDecimal("15.00"));
        rule.setWastePercentage(new BigDecimal("5.00"));
        rule.setMarginPercentage(new BigDecimal("50.00"));

        Finishing finishing = new Finishing();
        finishing.setId(1L);
        finishing.setName("Ilhós");
        finishing.setPricingType(FinishingPricingType.UNIT);
        finishing.setCost(new BigDecimal("0.75"));
        finishing.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(areaPricingRuleRepository.findByProductId(1L)).thenReturn(Optional.of(rule));
        when(finishingRepository.findById(1L)).thenReturn(Optional.of(finishing));

        AreaQuoteCalculationRequest request = new AreaQuoteCalculationRequest(
                1L, 1L, 2, new BigDecimal("1.00"), new BigDecimal("2.00"),
                List.of(new FinishingCalculationRequest(1L, 8))
        );

        // when
        QuoteCalculationResponse result = quoteCalculationService.calculate(request);

        // then
        assertThat(result.unitArea()).isEqualByComparingTo("2.00");
        assertThat(result.totalArea()).isEqualByComparingTo("4.00");
        assertThat(result.materialCost()).isEqualByComparingTo("74.00"); // 4 * 18.5
        assertThat(result.printingCost()).isEqualByComparingTo("40.00"); // 4 * 10
        assertThat(result.finishingCost()).isEqualByComparingTo("6.00"); // 8 * 0.75
        assertThat(result.wasteCost()).isEqualByComparingTo("5.70"); // (74+40) * 0.05
        assertThat(result.laborCost()).isEqualByComparingTo("15.00");
        
        // totalCost = 74.00 + 40.00 + 6.00 + 5.70 + 15.00 = 140.70
        assertThat(result.totalCost()).isEqualByComparingTo("140.70");
        
        // suggestedPrice = 140.70 / (1 - 0.5) = 281.40
        assertThat(result.suggestedPrice()).isEqualByComparingTo("281.40");
    }

    @Test
    @DisplayName("Should reject calculation when product is missing")
    void shouldRejectWhenProductIsMissing() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        AreaQuoteCalculationRequest request = new AreaQuoteCalculationRequest(1L, 1L, 2, BigDecimal.ONE, BigDecimal.ONE, null);

        assertThatThrownBy(() -> quoteCalculationService.calculate(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    @DisplayName("Should reject calculation when product is inactive")
    void shouldRejectWhenProductIsInactive() {
        Product product = new Product();
        product.setId(1L);
        product.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        AreaQuoteCalculationRequest request = new AreaQuoteCalculationRequest(1L, 1L, 2, BigDecimal.ONE, BigDecimal.ONE, null);

        assertThatThrownBy(() -> quoteCalculationService.calculate(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Produto inativo");
    }

    @Test
    @DisplayName("Should reject calculation when material has wrong UnitMeasure for AREA")
    void shouldRejectWhenMaterialHasWrongUnitMeasureForArea() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.AREA);
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Material material = new Material();
        material.setId(1L);
        material.setUnitMeasure(UnitMeasure.SHEET); // Invalid for AREA
        material.setActive(true);
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        AreaQuoteCalculationRequest request = new AreaQuoteCalculationRequest(1L, 1L, 2, BigDecimal.ONE, BigDecimal.ONE, null);

        assertThatThrownBy(() -> quoteCalculationService.calculate(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não é medido em m²");
    }

    // --- QUANTITY PRICING TESTS ---

    @Test
    @DisplayName("Should calculate QUANTITY quote correctly without finishings")
    void shouldCalculateQuantityQuoteCorrectly() {
        // given
        Product product = new Product();
        product.setId(1L);
        product.setName("Cartão de visita");
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        Material material = new Material();
        material.setId(1L);
        material.setName("Couché 300g");
        material.setUnitMeasure(UnitMeasure.SHEET);
        material.setCost(new BigDecimal("1.75"));
        material.setActive(true);

        QuantityPricingRule rule = new QuantityPricingRule();
        rule.setActive(true);
        rule.setUnitsPerSheet(20);
        rule.setPrintingCostPerUnit(new BigDecimal("0.06"));
        rule.setLaborCost(new BigDecimal("15.00"));
        rule.setWastePercentage(new BigDecimal("5.00"));
        rule.setMarginPercentage(new BigDecimal("50.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(quantityPricingRuleRepository.findByProductId(1L)).thenReturn(Optional.of(rule));

        QuantityQuoteCalculationRequest request = new QuantityQuoteCalculationRequest(1L, 1L, 1000, null);

        // when
        QuantityQuoteCalculationResponse result = quoteCalculationService.calculateQuantity(request);

        // then
        assertThat(result.requiredSheets()).isEqualTo(50); // 1000 / 20 = 50
        assertThat(result.materialCost()).isEqualByComparingTo("87.50"); // 50 * 1.75
        assertThat(result.printingCost()).isEqualByComparingTo("60.00"); // 1000 * 0.06
        assertThat(result.wasteCost()).isEqualByComparingTo("7.38"); // (87.5 + 60) * 0.05 = 7.375 -> 7.38
        assertThat(result.laborCost()).isEqualByComparingTo("15.00");
        
        // totalCost = 87.50 + 60.00 + 7.38 + 15.00 = 169.88
        assertThat(result.totalCost()).isEqualByComparingTo("169.88");
        
        // suggestedPrice = 169.88 / (1 - 0.5) = 339.76
        assertThat(result.suggestedPrice()).isEqualByComparingTo("339.76");
    }

    @Test
    @DisplayName("Should round requiredSheets up when quantity is not divisible by unitsPerSheet")
    void shouldRoundRequiredSheetsUpWhenQuantityIsNotDivisible() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        Material material = new Material();
        material.setId(1L);
        material.setUnitMeasure(UnitMeasure.SHEET);
        material.setCost(new BigDecimal("1.75"));
        material.setActive(true);

        QuantityPricingRule rule = new QuantityPricingRule();
        rule.setActive(true);
        rule.setUnitsPerSheet(20);
        rule.setPrintingCostPerUnit(new BigDecimal("0.06"));
        rule.setLaborCost(new BigDecimal("15.00"));
        rule.setWastePercentage(new BigDecimal("5.00"));
        rule.setMarginPercentage(new BigDecimal("50.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(quantityPricingRuleRepository.findByProductId(1L)).thenReturn(Optional.of(rule));

        QuantityQuoteCalculationRequest request = new QuantityQuoteCalculationRequest(1L, 1L, 1001, null);

        QuantityQuoteCalculationResponse result = quoteCalculationService.calculateQuantity(request);

        assertThat(result.requiredSheets()).isEqualTo(51); // 1001 / 20 = 50.05 -> 51
    }

    @Test
    @DisplayName("Should round requiredSheets up correctly for a small quantity")
    void shouldRoundRequiredSheetsUpForSmallQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        Material material = new Material();
        material.setId(1L);
        material.setUnitMeasure(UnitMeasure.SHEET);
        material.setCost(new BigDecimal("1.75"));
        material.setActive(true);

        QuantityPricingRule rule = new QuantityPricingRule();
        rule.setActive(true);
        rule.setUnitsPerSheet(20);
        rule.setPrintingCostPerUnit(new BigDecimal("0.06"));
        rule.setLaborCost(new BigDecimal("15.00"));
        rule.setWastePercentage(new BigDecimal("5.00"));
        rule.setMarginPercentage(new BigDecimal("50.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(quantityPricingRuleRepository.findByProductId(1L)).thenReturn(Optional.of(rule));

        QuantityQuoteCalculationRequest request = new QuantityQuoteCalculationRequest(1L, 1L, 1, null);

        QuantityQuoteCalculationResponse result = quoteCalculationService.calculateQuantity(request);

        assertThat(result.requiredSheets()).isEqualTo(1); // 1 / 20 = 0.05 -> 1
    }

    @Test
    @DisplayName("Should reject AREA finishing for QUANTITY quote")
    void shouldRejectAreaFinishingForQuantityQuote() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        Material material = new Material();
        material.setId(1L);
        material.setUnitMeasure(UnitMeasure.SHEET);
        material.setCost(new BigDecimal("1.75"));
        material.setActive(true);

        QuantityPricingRule rule = new QuantityPricingRule();
        rule.setActive(true);
        rule.setUnitsPerSheet(20);
        rule.setPrintingCostPerUnit(new BigDecimal("0.06"));
        rule.setLaborCost(new BigDecimal("15.00"));
        rule.setWastePercentage(new BigDecimal("5.00"));
        rule.setMarginPercentage(new BigDecimal("50.00"));

        Finishing finishing = new Finishing();
        finishing.setId(1L);
        finishing.setName("Verniz por metro");
        finishing.setPricingType(FinishingPricingType.AREA);
        finishing.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(quantityPricingRuleRepository.findByProductId(1L)).thenReturn(Optional.of(rule));
        when(finishingRepository.findById(1L)).thenReturn(Optional.of(finishing));

        QuantityQuoteCalculationRequest request = new QuantityQuoteCalculationRequest(1L, 1L, 1000, List.of(new FinishingCalculationRequest(1L, 1)));

        assertThatThrownBy(() -> quoteCalculationService.calculateQuantity(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não é suportado em orçamentos QUANTITY");
    }
}
