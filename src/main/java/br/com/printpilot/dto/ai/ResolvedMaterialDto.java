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
public class ResolvedMaterialDto {
    private CatalogResolutionStatus status;
    private Long id;
    private String name;
}
