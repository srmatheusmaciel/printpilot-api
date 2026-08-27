package br.com.printpilot.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInterpretRequest {

    @NotBlank(message = "Text cannot be empty")
    @Size(max = 2000, message = "Text exceeds the maximum length of 2000 characters")
    private String text;

}
