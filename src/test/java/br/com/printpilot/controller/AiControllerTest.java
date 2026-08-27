package br.com.printpilot.controller;

import br.com.printpilot.dto.ai.AiInterpretRequest;
import br.com.printpilot.dto.ai.AiInterpretResponse;
import br.com.printpilot.enums.PricingType;
import br.com.printpilot.service.AiInterpretationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AiControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AiInterpretationService aiInterpretationService;

    @InjectMocks
    private AiController aiController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    @Test
    public void testInterpretValidRequest() throws Exception {
        AiInterpretRequest request = new AiInterpretRequest("Quero 2 banners");
        AiInterpretResponse response = AiInterpretResponse.builder()
                .product("Banner")
                .pricingType(PricingType.AREA)
                .quantity(2)
                .build();

        when(aiInterpretationService.interpret(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/ai/interpret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Banner"))
                .andExpect(jsonPath("$.pricingType").value("AREA"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    public void testInterpretEmptyText() throws Exception {
        AiInterpretRequest request = new AiInterpretRequest("");

        mockMvc.perform(post("/api/ai/interpret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInterpretMissingField() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/ai/interpret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInterpretTextTooLong() throws Exception {
        String longText = "a".repeat(2001);
        AiInterpretRequest request = new AiInterpretRequest(longText);

        mockMvc.perform(post("/api/ai/interpret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
