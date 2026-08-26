package br.com.printpilot.controller;

import br.com.printpilot.dto.material.MaterialRequest;
import br.com.printpilot.dto.material.MaterialResponse;
import br.com.printpilot.service.MaterialService;
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

@Tag(name = "Materials", description = "Gerenciamento de materiais")
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar material", description = "Cria um novo material utilizado pela gráfica.")
    public MaterialResponse create(@Valid @RequestBody MaterialRequest request) {
        return service.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar materiais", description = "Lista todos os materiais cadastrados.")
    public List<MaterialResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar material", description = "Consulta um material pelo ID.")
    public MaterialResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar material", description = "Atualiza um material existente.")
    public MaterialResponse update(@PathVariable Long id, @Valid @RequestBody MaterialRequest request) {
        return service.update(id, request);
    }
}
