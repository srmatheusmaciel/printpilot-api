package br.com.printpilot.material;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository repository;

    @Transactional
    public MaterialResponse create(MaterialRequest request) {
        Material material = Material.builder()
                .name(request.name())
                .unitMeasure(request.unitMeasure())
                .cost(request.cost())
                .active(request.active() != null ? request.active() : true)
                .build();

        Material saved = repository.save(material);
        return MaterialResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> findAll() {
        return repository.findAll().stream()
                .map(MaterialResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse findById(Long id) {
        Material material = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));
        return MaterialResponse.fromEntity(material);
    }

    @Transactional
    public MaterialResponse update(Long id, MaterialRequest request) {
        Material material = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));

        material.setName(request.name());
        material.setUnitMeasure(request.unitMeasure());
        material.setCost(request.cost());
        if (request.active() != null) {
            material.setActive(request.active());
        }

        Material updated = repository.save(material);
        return MaterialResponse.fromEntity(updated);
    }
}
