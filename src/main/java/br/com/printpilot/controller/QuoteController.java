package br.com.printpilot.controller;

import br.com.printpilot.dto.quote.CreateQuoteRequest;
import br.com.printpilot.dto.quote.QuoteResponse;
import br.com.printpilot.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse create(@Valid @RequestBody CreateQuoteRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<QuoteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public QuoteResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
