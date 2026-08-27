package br.com.printpilot.service;

import br.com.printpilot.dto.pricing.QuantityPricingRuleRequest;
import br.com.printpilot.dto.pricing.QuantityPricingRuleResponse;
import br.com.printpilot.entity.Product;
import br.com.printpilot.entity.QuantityPricingRule;
import br.com.printpilot.enums.PricingType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuantityPricingRuleServiceTest {

    @Mock
    private QuantityPricingRuleRepository repository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private QuantityPricingRuleService service;

    @Test
    @DisplayName("Should create QuantityPricingRule for active QUANTITY product")
    void shouldCreateRuleForActiveQuantityProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.existsByProductId(1L)).thenReturn(false);

        QuantityPricingRule savedRule = new QuantityPricingRule();
        savedRule.setId(1L);
        savedRule.setProduct(product);
        savedRule.setUnitsPerSheet(20);
        savedRule.setPrintingCostPerUnit(new BigDecimal("0.06"));

        when(repository.save(any(QuantityPricingRule.class))).thenReturn(savedRule);

        QuantityPricingRuleRequest request = new QuantityPricingRuleRequest(
                1L, 20, new BigDecimal("0.06"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true
        );

        QuantityPricingRuleResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.unitsPerSheet()).isEqualTo(20);
        assertThat(response.printingCostPerUnit()).isEqualByComparingTo("0.06");
    }

    @Test
    @DisplayName("Should reject rule creation if product is AREA")
    void shouldRejectIfProductIsArea() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.AREA);
        product.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        QuantityPricingRuleRequest request = new QuantityPricingRuleRequest(
                1L, 20, new BigDecimal("0.06"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Produto deve possuir PricingType.QUANTITY");
    }

    @Test
    @DisplayName("Should reject rule creation if rule already exists for product")
    void shouldRejectIfRuleAlreadyExists() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        when(repository.existsByProductId(1L)).thenReturn(true);

        QuantityPricingRuleRequest request = new QuantityPricingRuleRequest(
                1L, 20, new BigDecimal("0.06"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Produto já possui uma regra QUANTITY cadastrada");
    }
}
