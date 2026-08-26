package br.com.printpilot.service;

import br.com.printpilot.dto.finishing.FinishingRequest;
import br.com.printpilot.dto.finishing.FinishingResponse;
import br.com.printpilot.entity.Finishing;
import br.com.printpilot.repository.FinishingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinishingService {

    private final FinishingRepository repository;

    @Transactional
    public FinishingResponse create(FinishingRequest request) {
        Finishing finishing = Finishing.builder()
                .name(request.name())
                .description(request.description())
                .pricingType(request.pricingType())
                .cost(request.cost())
                .active(request.active() != null ? request.active() : true)
                .build();

        Finishing saved = repository.save(finishing);
        return FinishingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<FinishingResponse> findAll() {
        return repository.findAll().stream()
                .map(FinishingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public FinishingResponse findById(Long id) {
        Finishing finishing = findEntityById(id);
        return FinishingResponse.fromEntity(finishing);
    }

    @Transactional
    public FinishingResponse update(Long id, FinishingRequest request) {
        Finishing finishing = findEntityById(id);

        finishing.setName(request.name());
        finishing.setDescription(request.description());
        finishing.setPricingType(request.pricingType());
        finishing.setCost(request.cost());
        if (request.active() != null) {
            finishing.setActive(request.active());
        }

        Finishing updated = repository.save(finishing);
        return FinishingResponse.fromEntity(updated);
    }

    private Finishing findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acabamento não encontrado"));
    }
}
