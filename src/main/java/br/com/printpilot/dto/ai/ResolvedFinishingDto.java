package br.com.printpilot.dto.ai;

import br.com.printpilot.enums.CatalogResolutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedFinishingDto {
    private String requestedName;
    private Integer requestedQuantity;
    private CatalogResolutionStatus status;
    private Long id;
    private String name;
}
