package br.com.printpilot.service;

import br.com.printpilot.dto.ai.CatalogCandidateDto;
import br.com.printpilot.dto.ai.CatalogToolResponse;
import br.com.printpilot.enums.CatalogResolutionStatus;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.enums.UnitMeasure;
import br.com.printpilot.entity.Product;
import br.com.printpilot.entity.Material;
import br.com.printpilot.entity.Finishing;
import br.com.printpilot.repository.ProductRepository;
import br.com.printpilot.repository.MaterialRepository;
import br.com.printpilot.repository.FinishingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogTools {

    private final ProductRepository productRepository;
    private final MaterialRepository materialRepository;
    private final FinishingRepository finishingRepository;

    public CatalogToolResponse resolveProduct(String name, String pricingTypeStr) {
        if (name == null || name.trim().isEmpty()) {
            return CatalogToolResponse.builder().status(CatalogResolutionStatus.NOT_FOUND).build();
        }

        PricingType requestedPricingType = null;
        if (pricingTypeStr != null && !pricingTypeStr.trim().isEmpty()) {
            try {
                requestedPricingType = PricingType.valueOf(pricingTypeStr.toUpperCase());
            } catch (Exception e) {
                // Ignore invalid pricing type
            }
        }

        final PricingType finalRequestedPricingType = requestedPricingType;

        List<Product> products = productRepository.findAll().stream()
                .filter(Product::getActive)
                .filter(product -> finalRequestedPricingType == null
                        || product.getPricingType() == finalRequestedPricingType)
                .collect(Collectors.toList());

        String normalizedRequestedName = normalize(name);

        // Try exact match
        List<Product> exactMatches = products.stream()
                .filter(p -> normalize(p.getName()).equals(normalizedRequestedName))
                .collect(Collectors.toList());

        if (exactMatches.size() == 1) {
            Product matched = exactMatches.get(0);

            return createResponse(
                    CatalogResolutionStatus.RESOLVED,
                    matched.getId(),
                    matched.getName(),
                    List.of());
        } else if (exactMatches.size() > 1) {
            return createAmbigousResponse(exactMatches.stream()
                    .map(p -> new CatalogCandidateDto(p.getId(), p.getName(), p.getPricingType()))
                    .collect(Collectors.toList()));
        }

        // Try partial match
        List<Product> partialMatches = products.stream()
                .filter(p -> normalize(p.getName()).contains(normalizedRequestedName)
                        || normalizedRequestedName.contains(normalize(p.getName())))
                .collect(Collectors.toList());

        if (partialMatches.size() == 1) {
            Product matched = partialMatches.get(0);

            return createResponse(
                    CatalogResolutionStatus.RESOLVED,
                    matched.getId(),
                    matched.getName(),
                    List.of());
        } else if (partialMatches.size() > 1) {
            return createAmbigousResponse(partialMatches.stream()
                    .map(p -> new CatalogCandidateDto(p.getId(), p.getName(), p.getPricingType()))
                    .collect(Collectors.toList()));
        }

        return CatalogToolResponse.builder().status(CatalogResolutionStatus.NOT_FOUND).build();
    }

    public CatalogToolResponse resolveMaterial(String name, String pricingTypeStr) {
        if (name == null || name.trim().isEmpty()) {
            return CatalogToolResponse.builder().status(CatalogResolutionStatus.NOT_FOUND).build();
        }

        PricingType requestedPricingType = null;
        if (pricingTypeStr != null && !pricingTypeStr.trim().isEmpty()) {
            try {
                requestedPricingType = PricingType.valueOf(pricingTypeStr.toUpperCase());
            } catch (Exception e) {
                // Ignore
            }
        }

        List<Material> materials = materialRepository.findAll().stream()
                .filter(Material::getActive)
                .collect(Collectors.toList());

        final PricingType finalPricingType = requestedPricingType;

        // Filter based on PricingType
        if (finalPricingType != null) {
            materials = materials.stream()
                    .filter(material -> {
                        if (finalPricingType == PricingType.AREA) {
                            return material.getUnitMeasure() == UnitMeasure.SQUARE_METER;
                        }

                        if (finalPricingType == PricingType.QUANTITY) {
                            return material.getUnitMeasure() == UnitMeasure.SHEET;
                        }

                        return true;
                    })
                    .collect(Collectors.toList());
        }

        String normalizedRequestedName = normalize(name);

        List<Material> exactMatches = materials.stream()
                .filter(m -> normalize(m.getName()).equals(normalizedRequestedName))
                .collect(Collectors.toList());

        if (exactMatches.size() == 1) {
            Material matched = exactMatches.get(0);
            return createResponse(CatalogResolutionStatus.RESOLVED, matched.getId(), matched.getName(), List.of());
        } else if (exactMatches.size() > 1) {
            return createAmbigousResponse(exactMatches.stream()
                    .map(m -> new CatalogCandidateDto(m.getId(), m.getName(), null))
                    .collect(Collectors.toList()));
        }

        List<Material> partialMatches = materials.stream()
                .filter(m -> normalize(m.getName()).contains(normalizedRequestedName)
                        || normalizedRequestedName.contains(normalize(m.getName())))
                .collect(Collectors.toList());

        if (partialMatches.size() == 1) {
            Material matched = partialMatches.get(0);
            return createResponse(CatalogResolutionStatus.RESOLVED, matched.getId(), matched.getName(), List.of());
        } else if (partialMatches.size() > 1) {
            return createAmbigousResponse(partialMatches.stream()
                    .map(m -> new CatalogCandidateDto(m.getId(), m.getName(), null))
                    .collect(Collectors.toList()));
        }

        return CatalogToolResponse.builder().status(CatalogResolutionStatus.NOT_FOUND).build();
    }

    public CatalogToolResponse resolveFinishing(String name) {
        if (name == null || name.trim().isEmpty()) {
            return CatalogToolResponse.builder().status(CatalogResolutionStatus.NOT_FOUND).build();
        }

        List<Finishing> finishings = finishingRepository.findAll().stream()
                .filter(Finishing::getActive)
                .collect(Collectors.toList());

        String normalizedRequestedName = normalize(name);

        List<Finishing> exactMatches = finishings.stream()
                .filter(f -> normalize(f.getName()).equals(normalizedRequestedName))
                .collect(Collectors.toList());

        if (exactMatches.size() == 1) {
            Finishing matched = exactMatches.get(0);
            return createResponse(CatalogResolutionStatus.RESOLVED, matched.getId(), matched.getName(), List.of());
        } else if (exactMatches.size() > 1) {
            return createAmbigousResponse(exactMatches.stream()
                    .map(f -> new CatalogCandidateDto(f.getId(), f.getName(), null))
                    .collect(Collectors.toList()));
        }

        List<Finishing> partialMatches = finishings.stream()
                .filter(f -> normalize(f.getName()).contains(normalizedRequestedName)
                        || normalizedRequestedName.contains(normalize(f.getName())))
                .collect(Collectors.toList());

        if (partialMatches.size() == 1) {
            Finishing matched = partialMatches.get(0);
            return createResponse(CatalogResolutionStatus.RESOLVED, matched.getId(), matched.getName(), List.of());
        } else if (partialMatches.size() > 1) {
            return createAmbigousResponse(partialMatches.stream()
                    .map(f -> new CatalogCandidateDto(f.getId(), f.getName(), null))
                    .collect(Collectors.toList()));
        }

        return CatalogToolResponse.builder().status(CatalogResolutionStatus.NOT_FOUND).build();
    }

    private String normalize(String input) {
        if (input == null)
            return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private CatalogToolResponse createResponse(CatalogResolutionStatus status, Long id, String name,
            List<CatalogCandidateDto> candidates) {
        return CatalogToolResponse.builder()
                .status(status)
                .id(id)
                .name(name)
                .candidates(candidates)
                .build();
    }

    private CatalogToolResponse createAmbigousResponse(List<CatalogCandidateDto> candidates) {
        return CatalogToolResponse.builder()
                .status(CatalogResolutionStatus.AMBIGUOUS)
                .candidates(candidates)
                .build();
    }
}
