package br.com.printpilot.controller;

import br.com.printpilot.dto.finishing.FinishingRequest;
import br.com.printpilot.dto.finishing.FinishingResponse;
import br.com.printpilot.service.FinishingService;
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

@Tag(name = "Finishings", description = "Gerenciamento de acabamentos")
@RestController
@RequestMapping("/api/finishings")
@RequiredArgsConstructor
public class FinishingController {

    private final FinishingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar acabamento", description = "Cria um novo acabamento utilizado pela gráfica.")
    public FinishingResponse create(@Valid @RequestBody FinishingRequest request) {
        return service.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar acabamentos", description = "Lista todos os acabamentos cadastrados.")
    public List<FinishingResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar acabamento", description = "Consulta um acabamento pelo ID.")
    public FinishingResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar acabamento", description = "Atualiza um acabamento existente.")
    public FinishingResponse update(@PathVariable Long id, @Valid @RequestBody FinishingRequest request) {
        return service.update(id, request);
    }
}
