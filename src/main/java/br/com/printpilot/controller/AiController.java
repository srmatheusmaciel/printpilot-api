package br.com.printpilot.controller;

import br.com.printpilot.dto.ai.AiInterpretRequest;
import br.com.printpilot.dto.ai.AiInterpretResponse;
import br.com.printpilot.service.AiInterpretationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI Integration endpoints for natural-language interpretation")
public class AiController {

    private final AiInterpretationService aiInterpretationService;

    @PostMapping("/interpret")
    @Operation(
            summary = "Interpret print request text",
            description = "Interprets a natural-language print request into structured quote specifications. " +
                          "This endpoint does not calculate prices and does not persist quotes."
    )
    public ResponseEntity<AiInterpretResponse> interpret(@Valid @RequestBody AiInterpretRequest request) {
        AiInterpretResponse response = aiInterpretationService.interpret(request.getText());
        return ResponseEntity.ok(response);
    }
}
