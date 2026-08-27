package br.com.printpilot.controller;

import br.com.printpilot.dto.quote.CreateQuoteRequest;
import br.com.printpilot.dto.quote.QuoteResponse;
import br.com.printpilot.dto.quote.UpdateQuoteFinalPriceRequest;
import br.com.printpilot.dto.quote.UpdateQuoteStatusRequest;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.enums.QuoteStatus;
import br.com.printpilot.service.QuotePdfService;
import br.com.printpilot.service.QuoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QuoteControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private QuoteService quoteService;
    
    @Mock
    private QuotePdfService quotePdfService;

    @InjectMocks
    private QuoteController quoteController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(quoteController).build();
    }

    @Test
    @DisplayName("Should return 201 Created when creating an AREA quote successfully")
    void shouldCreateAreaQuote() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                null, 1L, 1L, 10, new BigDecimal("1.0"), new BigDecimal("2.0"), null
        );

        QuoteResponse response = new QuoteResponse(
                1L, null, null, 1L, "Banner", 1L, "Lona", PricingType.AREA,
                10, new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("20.0"),
                null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, QuoteStatus.DRAFT,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(quoteService.create(any(CreateQuoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request is invalid")
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        // quantity is null, invalid
        CreateQuoteRequest request = new CreateQuoteRequest(
                null, 1L, null, null, new BigDecimal("1.0"), new BigDecimal("2.0"), null
        );

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 OK when updating quote status")
    void shouldUpdateQuoteStatus() throws Exception {
        UpdateQuoteStatusRequest request = new UpdateQuoteStatusRequest(QuoteStatus.SENT);

        QuoteResponse response = new QuoteResponse(
                1L, null, null, 1L, "Banner", 1L, "Lona", PricingType.AREA,
                10, new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("20.0"),
                null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, QuoteStatus.SENT,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(quoteService.updateStatus(eq(1L), any(UpdateQuoteStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/quotes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when transition is invalid")
    void shouldReturnConflictWhenTransitionIsInvalid() throws Exception {
        UpdateQuoteStatusRequest request = new UpdateQuoteStatusRequest(QuoteStatus.APPROVED);

        when(quoteService.updateStatus(eq(1L), any(UpdateQuoteStatusRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Transição de status inválida"));

        mockMvc.perform(patch("/api/quotes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 200 OK when updating final price")
    void shouldUpdateFinalPrice() throws Exception {
        UpdateQuoteFinalPriceRequest request = new UpdateQuoteFinalPriceRequest(new BigDecimal("320.00"));

        QuoteResponse response = new QuoteResponse(
                1L, null, null, 1L, "Banner", 1L, "Lona", PricingType.AREA,
                10, new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("20.0"),
                null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.TEN, new BigDecimal("350.00"), new BigDecimal("320.00"), QuoteStatus.DRAFT,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(quoteService.updateFinalPrice(eq(1L), any(UpdateQuoteFinalPriceRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/quotes/1/final-price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalPrice").value(320.00));
    }
}
