package br.com.printpilot.dto.ai;

import br.com.printpilot.enums.CatalogResolutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogToolResponse {
    private CatalogResolutionStatus status;
    private Long id;
    private String name;
    private List<CatalogCandidateDto> candidates;
}
