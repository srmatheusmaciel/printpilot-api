package br.com.printpilot.service;

import br.com.printpilot.dto.ai.CatalogResolveResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogResolveServiceTest {

    @Mock
    private CatalogResolveAgent catalogResolveAgent;

    @InjectMocks
    private CatalogResolveService catalogResolveService;

    @Test
    void shouldReturnCatalogResolutionWhenAgentSucceeds() {
        String text = "Quero um banner em lona 440g";

        CatalogResolveResponse expectedResponse = mock(CatalogResolveResponse.class);

        when(catalogResolveAgent.resolve(text))
                .thenReturn(expectedResponse);

        CatalogResolveResponse result = catalogResolveService.resolveCatalog(text);

        assertSame(expectedResponse, result);
    }

    @Test
    void shouldReturnTooManyRequestsWhenProviderReturns429() {
        String text = "Quero um banner";

        RuntimeException providerException = new RuntimeException("429 RESOURCE_EXHAUSTED");

        RuntimeException agentException = new RuntimeException(
                "Error communicating with AI Provider",
                providerException);

        when(catalogResolveAgent.resolve(text))
                .thenThrow(agentException);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> catalogResolveService.resolveCatalog(text));

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getStatusCode());

        assertEquals(
                "AI Provider limit reached",
                exception.getReason());
    }

    @Test
    void shouldReturnBadRequestWhenProviderReturns400() {
        String text = "Pedido inválido";

        RuntimeException providerException = new RuntimeException("400 INVALID_ARGUMENT");

        RuntimeException agentException = new RuntimeException(
                "Error communicating with AI Provider",
                providerException);

        when(catalogResolveAgent.resolve(text))
                .thenThrow(agentException);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> catalogResolveService.resolveCatalog(text));

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode());

        assertEquals(
                "Invalid request sent to AI Provider",
                exception.getReason());
    }

    @Test
    void shouldReturnBadGatewayForUnexpectedProviderError() {
        String text = "Quero um banner";

        RuntimeException providerException = new RuntimeException("Unexpected provider error");

        RuntimeException agentException = new RuntimeException(
                "Error communicating with AI Provider",
                providerException);

        when(catalogResolveAgent.resolve(text))
                .thenThrow(agentException);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> catalogResolveService.resolveCatalog(text));

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatusCode());

        assertEquals(
                "Error communicating with AI provider",
                exception.getReason());
    }
}