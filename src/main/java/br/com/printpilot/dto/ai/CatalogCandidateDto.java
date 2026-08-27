package br.com.printpilot.dto.ai;

import br.com.printpilot.enums.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogCandidateDto {
    private Long id;
    private String name;
    private PricingType pricingType;
}
