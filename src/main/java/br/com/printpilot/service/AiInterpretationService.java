package br.com.printpilot.service;

import br.com.printpilot.dto.ai.AiInterpretResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AiInterpretationService {

    private final QuoteInterpreterAgent quoteInterpreterAgent;

    public AiInterpretResponse interpret(String text) {
        try {
            AiInterpretResponse aiResponse = quoteInterpreterAgent.interpret(text);
            validateResponse(aiResponse);
            return aiResponse;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errorMessage = e.getCause().getMessage();
            }
            if (errorMessage != null && errorMessage.contains("429")) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "AI Provider limit reached. Please try again later.", e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error communicating with AI provider", e);
        }
    }

    private void validateResponse(AiInterpretResponse response) {
        if (response.getQuantity() != null && response.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
        if (response.getWidth() != null && response.getWidth().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Width must be greater than zero");
        }
        if (response.getHeight() != null && response.getHeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Height must be greater than zero");
        }
    }
}
