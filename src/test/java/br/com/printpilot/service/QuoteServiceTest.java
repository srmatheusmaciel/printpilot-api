package br.com.printpilot.service;

import br.com.printpilot.dto.quote.AreaQuoteCalculationRequest;
import br.com.printpilot.dto.quote.CreateQuoteRequest;
import br.com.printpilot.dto.quote.QuoteCalculationResponse;
import br.com.printpilot.dto.quote.QuoteResponse;
import br.com.printpilot.dto.quote.UpdateQuoteFinalPriceRequest;
import br.com.printpilot.dto.quote.UpdateQuoteStatusRequest;
import br.com.printpilot.entity.Material;
import br.com.printpilot.entity.Product;
import br.com.printpilot.entity.Quote;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.enums.QuoteStatus;
import br.com.printpilot.repository.CustomerRepository;
import br.com.printpilot.repository.MaterialRepository;
import br.com.printpilot.repository.ProductRepository;
import br.com.printpilot.repository.QuoteRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteCalculationService calculationService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private QuoteService quoteService;

    @Test
    @DisplayName("Should create quote and set status to DRAFT with finalPrice = suggestedPrice")
    void shouldCreateQuoteAndSetInitialValues() {
        // given
        CreateQuoteRequest request = new CreateQuoteRequest(null, 1L, 1L, 2, BigDecimal.ONE, BigDecimal.ONE, null);

        Product product = new Product();
        product.setId(1L);
        product.setName("Banner");

        Material material = new Material();
        material.setId(1L);
        material.setName("Lona");

        QuoteCalculationResponse calcResponse = new QuoteCalculationResponse(
                1L, "Banner", 2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("2.00"),
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN,
                new BigDecimal("30.00"), new BigDecimal("50.00"), new BigDecimal("60.00")
        );

        when(calculationService.calculate(any(AreaQuoteCalculationRequest.class))).thenReturn(calcResponse);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        Quote savedQuote = Quote.builder()
                .id(1L)
                .product(product)
                .material(material)
                .productName("Banner")
                .materialName("Lona")
                .status(QuoteStatus.DRAFT)
                .suggestedPrice(new BigDecimal("60.00"))
                .finalPrice(new BigDecimal("60.00"))
                .pricingType(PricingType.AREA)
                .build();
        when(quoteRepository.save(any(Quote.class))).thenReturn(savedQuote);

        // when
        QuoteResponse result = quoteService.create(request);

        // then
        assertThat(result.status()).isEqualTo(QuoteStatus.DRAFT);
        assertThat(result.suggestedPrice()).isEqualByComparingTo("60.00");
        assertThat(result.finalPrice()).isEqualByComparingTo("60.00");
        assertThat(result.pricingType()).isEqualTo(PricingType.AREA);
    }

    @Test
    @DisplayName("Should allow valid status transitions")
    void shouldAllowValidStatusTransitions() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setStatus(QuoteStatus.DRAFT);
        Product product = new Product();
        product.setId(1L);
        quote.setProduct(product);
        Material material = new Material();
        material.setId(1L);
        quote.setMaterial(material);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(i -> i.getArgument(0));

        // DRAFT -> SENT
        QuoteResponse result = quoteService.updateStatus(1L, new UpdateQuoteStatusRequest(QuoteStatus.SENT));
        assertThat(result.status()).isEqualTo(QuoteStatus.SENT);

        // SENT -> APPROVED
        quote.setStatus(QuoteStatus.SENT);
        result = quoteService.updateStatus(1L, new UpdateQuoteStatusRequest(QuoteStatus.APPROVED));
        assertThat(result.status()).isEqualTo(QuoteStatus.APPROVED);
    }

    @Test
    @DisplayName("Should reject direct transition from DRAFT to APPROVED")
    void shouldRejectDirectTransitionFromDraftToApproved() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setStatus(QuoteStatus.DRAFT);
        Product product = new Product();
        product.setId(1L);
        quote.setProduct(product);
        Material material = new Material();
        material.setId(1L);
        quote.setMaterial(material);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> quoteService.updateStatus(1L, new UpdateQuoteStatusRequest(QuoteStatus.APPROVED)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("Should return same quote without error on idempotent transition")
    void shouldReturnSameQuoteOnIdempotentTransition() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setStatus(QuoteStatus.DRAFT);
        Product product = new Product();
        product.setId(1L);
        quote.setProduct(product);
        Material material = new Material();
        material.setId(1L);
        quote.setMaterial(material);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

        QuoteResponse result = quoteService.updateStatus(1L, new UpdateQuoteStatusRequest(QuoteStatus.DRAFT));
        
        assertThat(result.status()).isEqualTo(QuoteStatus.DRAFT);
        // Verify save was not called since it's the same status
        verify(quoteRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Should keep suggestedPrice when finalPrice is negotiated in DRAFT")
    void shouldKeepSuggestedPriceWhenFinalPriceIsNegotiated() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setStatus(QuoteStatus.DRAFT);
        Product product = new Product();
        product.setId(1L);
        quote.setProduct(product);
        Material material = new Material();
        material.setId(1L);
        quote.setMaterial(material);
        quote.setSuggestedPrice(new BigDecimal("339.76"));
        quote.setFinalPrice(new BigDecimal("339.76"));
        
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(i -> i.getArgument(0));

        QuoteResponse result = quoteService.updateFinalPrice(1L, new UpdateQuoteFinalPriceRequest(new BigDecimal("320.00")));

        assertThat(result.suggestedPrice()).isEqualByComparingTo("339.76");
        assertThat(result.finalPrice()).isEqualByComparingTo("320.00");
    }

    @Test
    @DisplayName("Should reject finalPrice update in APPROVED status")
    void shouldRejectFinalPriceUpdateInApprovedStatus() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setStatus(QuoteStatus.APPROVED);
        Product product = new Product();
        product.setId(1L);
        quote.setProduct(product);
        Material material = new Material();
        material.setId(1L);
        quote.setMaterial(material);
        
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> quoteService.updateFinalPrice(1L, new UpdateQuoteFinalPriceRequest(new BigDecimal("320.00"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Não é permitido alterar o preço");
    }
}
