package br.com.printpilot.controller;

import br.com.printpilot.dto.ai.AiInterpretRequest;
import br.com.printpilot.dto.ai.AiInterpretResponse;
import br.com.printpilot.dto.ai.CatalogResolveResponse;
import br.com.printpilot.service.AiInterpretationService;
import br.com.printpilot.service.CatalogResolveService;
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
    private final CatalogResolveService catalogResolveService;

    @PostMapping("/interpret")
    @Operation(summary = "Interpret and resolve a request against the PrintPilot catalog", description = """
            Interprets a natural-language print request and resolves
            Product, Material and Finishing references against the
            active PrintPilot catalog using read-only Google ADK tools.

            Possible resolution states include:
            RESOLVED, AMBIGUOUS and NOT_FOUND.

            This endpoint does not calculate prices and does not persist quotes.
            """)
    public ResponseEntity<AiInterpretResponse> interpret(@Valid @RequestBody AiInterpretRequest request) {
        AiInterpretResponse response = aiInterpretationService.interpret(request.getText());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/catalog-resolve")
    @Operation(summary = "Resolve print request against catalog", description = "Interprets a print request and resolves product, material and "
            +
            "finishing references against the active PrintPilot catalog.\n\n" +
            "Does not calculate prices and does not persist quotes.")
    public ResponseEntity<CatalogResolveResponse> resolveCatalog(@Valid @RequestBody AiInterpretRequest request) {
        CatalogResolveResponse response = catalogResolveService.resolveCatalog(request.getText());
        return ResponseEntity.ok(response);
    }
}
