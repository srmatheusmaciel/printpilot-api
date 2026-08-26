package br.com.printpilot.dto.material;

import br.com.printpilot.enums.UnitMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MaterialRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String name,

        @NotNull(message = "Unidade de medida é obrigatória")
        UnitMeasure unitMeasure,

        @NotNull(message = "Custo é obrigatório")
        @DecimalMin(value = "0.0", message = "Custo não pode ser negativo")
        BigDecimal cost,

        Boolean active
) {
}
