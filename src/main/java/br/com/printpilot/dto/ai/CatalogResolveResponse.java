package br.com.printpilot.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResolveResponse {
    private AiInterpretResponse interpretation;
    private ResolvedProductDto product;
    private ResolvedMaterialDto material;

    @Builder.Default
    private List<ResolvedFinishingDto> finishings = new ArrayList<>();

    private boolean fullyResolved;
    
    @Builder.Default
    private List<String> unresolvedFields = new ArrayList<>();

    @Builder.Default
    private List<String> ambiguities = new ArrayList<>();
    
    public List<ResolvedFinishingDto> getFinishings() {
        if (finishings == null) return new ArrayList<>();
        return finishings;
    }
    
    public List<String> getUnresolvedFields() {
        if (unresolvedFields == null) return new ArrayList<>();
        return unresolvedFields;
    }
    
    public List<String> getAmbiguities() {
        if (ambiguities == null) return new ArrayList<>();
        return ambiguities;
    }
}
