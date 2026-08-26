package br.com.printpilot.controller;

import br.com.printpilot.dto.customer.CustomerRequest;
import br.com.printpilot.dto.customer.CustomerResponse;
import br.com.printpilot.dto.quote.QuoteResponse;
import br.com.printpilot.service.CustomerService;
import br.com.printpilot.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Customers", description = "Cadastro e gerenciamento de clientes")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final QuoteService quoteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar cliente", description = "Cria um novo cliente.")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes cadastrados.")
    public List<CustomerResponse> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cliente", description = "Consulta um cliente pelo ID.")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente.")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @GetMapping("/{id}/quotes")
    @Operation(summary = "Histórico de orçamentos", description = "Lista os orçamentos associados a este cliente.")
    public List<QuoteResponse> findQuotesByCustomerId(@PathVariable Long id) {
        // Primeiro verifica se o cliente existe
        customerService.findById(id);
        // Então busca os orçamentos
        return quoteService.findAllByCustomerId(id);
    }
}
