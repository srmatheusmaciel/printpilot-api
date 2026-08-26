package br.com.printpilot.service;

import br.com.printpilot.dto.customer.CustomerRequest;
import br.com.printpilot.dto.customer.CustomerResponse;
import br.com.printpilot.entity.Customer;
import br.com.printpilot.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.name())
                .document(request.document())
                .email(request.email())
                .phone(request.phone())
                .active(request.active())
                .build();

        Customer saved = repository.save(customer);
        return CustomerResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll().stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        Customer customer = findEntityById(id);
        return CustomerResponse.fromEntity(customer);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntityById(id);
        
        customer.setName(request.name());
        customer.setDocument(request.document());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        if (request.active() != null) {
            customer.setActive(request.active());
        }

        Customer updated = repository.save(customer);
        return CustomerResponse.fromEntity(updated);
    }

    protected Customer findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado: id=" + id));
    }
}
