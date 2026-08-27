package br.com.printpilot.service;

import br.com.printpilot.dto.pricing.AreaPricingRuleRequest;
import br.com.printpilot.dto.pricing.AreaPricingRuleResponse;
import br.com.printpilot.entity.AreaPricingRule;
import br.com.printpilot.entity.Product;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.repository.AreaPricingRuleRepository;
import br.com.printpilot.repository.ProductRepository;
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
class AreaPricingRuleServiceTest {

    @Mock
    private AreaPricingRuleRepository repository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AreaPricingRuleService service;

    @Test
    @DisplayName("Should create AreaPricingRule for active AREA product")
    void shouldCreateRuleForActiveAreaProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.AREA);
        product.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.existsByProductId(1L)).thenReturn(false);

        AreaPricingRule savedRule = new AreaPricingRule();
        savedRule.setId(1L);
        savedRule.setProduct(product);
        savedRule.setPrintingCostPerSquareMeter(new BigDecimal("10.00"));

        when(repository.save(any(AreaPricingRule.class))).thenReturn(savedRule);

        AreaPricingRuleRequest request = new AreaPricingRuleRequest(
                1L, new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true
        );

        AreaPricingRuleResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.printingCostPerSquareMeter()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Should reject rule creation if product is QUANTITY")
    void shouldRejectIfProductIsQuantity() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.QUANTITY);
        product.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        AreaPricingRuleRequest request = new AreaPricingRuleRequest(
                1L, new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não possui PricingType.AREA");
    }

    @Test
    @DisplayName("Should reject rule creation if rule already exists for product")
    void shouldRejectIfRuleAlreadyExists() {
        Product product = new Product();
        product.setId(1L);
        product.setPricingType(PricingType.AREA);
        product.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.existsByProductId(1L)).thenReturn(true);

        AreaPricingRuleRequest request = new AreaPricingRuleRequest(
                1L, new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Já existe uma regra de precificação");
    }
}
