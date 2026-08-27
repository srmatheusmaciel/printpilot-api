package br.com.printpilot.service;

import br.com.printpilot.dto.quote.AreaQuoteCalculationRequest;
import br.com.printpilot.dto.quote.CreateQuoteRequest;
import br.com.printpilot.dto.quote.QuoteCalculationResponse;
import br.com.printpilot.dto.quote.QuoteResponse;
import br.com.printpilot.entity.Customer;
import br.com.printpilot.repository.CustomerRepository;
import br.com.printpilot.entity.Material;
import br.com.printpilot.entity.Product;
import br.com.printpilot.entity.Quote;
import br.com.printpilot.enums.QuoteStatus;
import br.com.printpilot.repository.MaterialRepository;
import br.com.printpilot.repository.ProductRepository;
import br.com.printpilot.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteCalculationService calculationService;
    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public QuoteResponse create(CreateQuoteRequest request) {
        // Adapta request para o motor de cálculo (sem duplicar fórmulas)
        AreaQuoteCalculationRequest calcRequest = new AreaQuoteCalculationRequest(
                request.productId(),
                request.materialId(),
                request.quantity(),
                request.width(),
                request.height(),
                request.finishings()
        );

        // Delega todo o cálculo e validações de negócio ao Pricing Engine
        QuoteCalculationResponse calculated = calculationService.calculate(calcRequest);

        // Busca entidades para o relacionamento (já validadas pelo calculationService)
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado: id=" + request.productId()));

        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Material não encontrado: id=" + request.materialId()));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Cliente não encontrado: id=" + request.customerId()));
            
            if (!customer.getActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cliente inativo não pode criar orçamentos: id=" + request.customerId());
            }
        }

        // Constrói Quote copiando snapshots e valores calculados
        Quote quote = Quote.builder()
                .pricingType(br.com.printpilot.enums.PricingType.AREA)
                .product(product)
                .material(material)
                .customer(customer)
                // Snapshots de nome — imutáveis para orçamentos históricos
                .productName(product.getName())
                .materialName(material.getName())
                // Dados de entrada
                .quantity(calculated.quantity())
                .width(calculated.width())
                .height(calculated.height())
                // Dimensões calculadas
                .unitArea(calculated.unitArea())
                .totalArea(calculated.totalArea())
                // Componentes financeiros calculados pelo Pricing Engine
                .materialCost(calculated.materialCost())
                .printingCost(calculated.printingCost())
                .finishingCost(calculated.finishingCost())
                .wasteCost(calculated.wasteCost())
                .laborCost(calculated.laborCost())
                .totalCost(calculated.totalCost())
                .marginPercentage(calculated.marginPercentage())
                .suggestedPrice(calculated.suggestedPrice())
                // finalPrice inicia igual ao suggestedPrice
                .finalPrice(calculated.suggestedPrice())
                // Status inicial sempre DRAFT
                .status(QuoteStatus.DRAFT)
                .build();

        Quote saved = quoteRepository.save(quote);
        return QuoteResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> findAll() {
        return quoteRepository.findAll().stream()
                .map(QuoteResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuoteResponse findById(Long id) {
        Quote quote = findEntityById(id);
        return QuoteResponse.fromEntity(quote);
    }

    private Quote findEntityById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orçamento não encontrado: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> findAllByCustomerId(Long customerId) {
        return quoteRepository.findAllByCustomerId(customerId).stream()
                .map(QuoteResponse::fromEntity)
                .toList();
    }

    @Transactional
    public QuoteResponse createQuantity(br.com.printpilot.dto.quote.CreateQuantityQuoteRequest request) {
        br.com.printpilot.dto.quote.QuantityQuoteCalculationRequest calcRequest = new br.com.printpilot.dto.quote.QuantityQuoteCalculationRequest(
                request.productId(),
                request.materialId(),
                request.quantity(),
                request.finishings()
        );

        br.com.printpilot.dto.quote.QuantityQuoteCalculationResponse calculated = calculationService.calculateQuantity(calcRequest);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado: id=" + request.productId()));

        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Material não encontrado: id=" + request.materialId()));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Cliente não encontrado: id=" + request.customerId()));

            if (!customer.getActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cliente inativo não pode criar orçamentos: id=" + request.customerId());
            }
        }

        Quote quote = Quote.builder()
                .pricingType(br.com.printpilot.enums.PricingType.QUANTITY)
                .product(product)
                .material(material)
                .customer(customer)
                .productName(product.getName())
                .materialName(material.getName())
                .quantity(calculated.quantity())
                .unitsPerSheet(calculated.unitsPerSheet())
                .requiredSheets(calculated.requiredSheets())
                .materialCost(calculated.materialCost())
                .printingCost(calculated.printingCost())
                .finishingCost(calculated.finishingCost())
                .wasteCost(calculated.wasteCost())
                .laborCost(calculated.laborCost())
                .totalCost(calculated.totalCost())
                .marginPercentage(calculated.marginPercentage())
                .suggestedPrice(calculated.suggestedPrice())
                .finalPrice(calculated.suggestedPrice())
                .status(QuoteStatus.DRAFT)
                .build();

        Quote saved = quoteRepository.save(quote);
        return QuoteResponse.fromEntity(saved);
    }

    @Transactional
    public QuoteResponse updateStatus(Long quoteId, br.com.printpilot.dto.quote.UpdateQuoteStatusRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orçamento não encontrado: id=" + quoteId));

        QuoteStatus currentStatus = quote.getStatus();
        QuoteStatus newStatus = request.status();

        if (currentStatus == newStatus) {
            return QuoteResponse.fromEntity(quote);
        }

        validateStatusTransition(currentStatus, newStatus);
        
        quote.setStatus(newStatus);
        Quote saved = quoteRepository.save(quote);
        return QuoteResponse.fromEntity(saved);
    }

    private void validateStatusTransition(QuoteStatus currentStatus, QuoteStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case DRAFT -> newStatus == QuoteStatus.SENT || newStatus == QuoteStatus.REJECTED || newStatus == QuoteStatus.EXPIRED;
            case SENT -> newStatus == QuoteStatus.APPROVED || newStatus == QuoteStatus.REJECTED || newStatus == QuoteStatus.EXPIRED;
            case APPROVED, REJECTED, EXPIRED -> false;
        };

        if (!isValid) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Transição de status inválida: " + currentStatus + " -> " + newStatus);
        }
    }

    @Transactional
    public QuoteResponse updateFinalPrice(Long quoteId, br.com.printpilot.dto.quote.UpdateQuoteFinalPriceRequest request) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orçamento não encontrado: id=" + quoteId));

        if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.SENT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é permitido alterar o preço de um orçamento no status: " + quote.getStatus());
        }

        quote.setFinalPrice(request.finalPrice());
        Quote saved = quoteRepository.save(quote);
        return QuoteResponse.fromEntity(saved);
    }
}
