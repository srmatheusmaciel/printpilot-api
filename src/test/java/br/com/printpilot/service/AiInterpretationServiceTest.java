package br.com.printpilot.service;

import br.com.printpilot.dto.ai.AiInterpretResponse;
import br.com.printpilot.enums.PricingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AiInterpretationServiceTest {

    @Mock
    private QuoteInterpreterAgent quoteInterpreterAgent;

    @InjectMocks
    private AiInterpretationService service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInterpretValidRequest() {
        AiInterpretResponse response = AiInterpretResponse.builder()
                .product("Banner")
                .pricingType(PricingType.AREA)
                .quantity(2)
                .width(new BigDecimal("1.0"))
                .height(new BigDecimal("2.0"))
                .build();

        when(quoteInterpreterAgent.interpret(anyString())).thenReturn(response);

        AiInterpretResponse result = service.interpret("Quero 2 banners de 1x2");

        assertEquals("Banner", result.getProduct());
        assertEquals(PricingType.AREA, result.getPricingType());
        assertEquals(2, result.getQuantity());
        assertEquals(1.0, result.getWidth().doubleValue());
    }

    @Test
    public void testInterpretInvalidQuantity() {
        AiInterpretResponse response = AiInterpretResponse.builder()
                .product("Banner")
                .pricingType(PricingType.AREA)
                .quantity(-5)
                .build();

        when(quoteInterpreterAgent.interpret(anyString())).thenReturn(response);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.interpret("teste");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void testProviderError() {
        when(quoteInterpreterAgent.interpret(anyString())).thenThrow(new RuntimeException("API error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.interpret("teste");
        });

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }
}
