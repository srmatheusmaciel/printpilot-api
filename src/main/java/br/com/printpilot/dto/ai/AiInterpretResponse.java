package br.com.printpilot.dto.ai;

import br.com.printpilot.enums.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInterpretResponse {
    private String product;
    private PricingType pricingType;
    private Integer quantity;
    private BigDecimal width;
    private BigDecimal height;
    private String material;

    @Builder.Default
    private List<AiFinishingInterpretation> finishings = new ArrayList<>();

    @Builder.Default
    private List<String> missingFields = new ArrayList<>();

    public List<AiFinishingInterpretation> getFinishings() {
        if (finishings == null) {
            return new ArrayList<>();
        }
        return finishings;
    }

    public List<String> getMissingFields() {
        if (missingFields == null) {
            return new ArrayList<>();
        }
        return missingFields;
    }
}
