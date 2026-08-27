package br.com.printpilot.dto.ai;

import br.com.printpilot.enums.CatalogResolutionStatus;
import br.com.printpilot.enums.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedProductDto {
    private CatalogResolutionStatus status;
    private Long id;
    private String name;
}
