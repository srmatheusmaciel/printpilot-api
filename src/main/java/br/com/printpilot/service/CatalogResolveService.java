package br.com.printpilot.service;

import br.com.printpilot.dto.ai.CatalogResolveResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogResolveService {

    private final CatalogResolveAgent catalogResolveAgent;

    public CatalogResolveService(CatalogResolveAgent catalogResolveAgent) {
        this.catalogResolveAgent = catalogResolveAgent;
    }

    public CatalogResolveResponse resolveCatalog(String text) {
        try {
            return catalogResolveAgent.resolve(text);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errorMessage = e.getCause().getMessage();
            }

            if (errorMessage != null) {
                if (errorMessage.contains("429")) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "AI Provider limit reached", e);
                }
                if (errorMessage.contains("400")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request sent to AI Provider", e);
                }
            }

            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error communicating with AI provider", e);
        }
    }
}
