package br.com.printpilot.service;

import br.com.printpilot.dto.product.ProductRequest;
import br.com.printpilot.dto.product.ProductResponse;
import br.com.printpilot.entity.Product;
import br.com.printpilot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .pricingType(request.pricingType())
                .active(request.active() != null ? request.active() : true)
                .build();

        Product saved = repository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = findEntityById(id);
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntityById(id);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPricingType(request.pricingType());
        if (request.active() != null) {
            product.setActive(request.active());
        }

        Product updated = repository.save(product);
        return ProductResponse.fromEntity(updated);
    }

    private Product findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }
}
