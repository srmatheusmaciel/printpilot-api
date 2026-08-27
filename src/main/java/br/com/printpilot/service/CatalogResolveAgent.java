package br.com.printpilot.service;

import br.com.printpilot.dto.ai.CatalogResolveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CatalogResolveAgent {

        private final LlmAgent agent;
        private final Runner runner;
        private final ObjectMapper objectMapper;

        private static final String SYSTEM_PROMPT = "Você interpreta pedidos de gráfica e pode utilizar Tools " +
                        "somente para consultar o catálogo do PrintPilot. " +
                        "Nunca invente IDs. IDs de Product, Material ou Finishing só podem ser utilizados " +
                        "quando retornados por uma Tool. Se a Tool retornar AMBIGUOUS, preserve a ambiguidade no response, "
                        +
                        "preenchendo a lista ambiguities com o nome do campo ambíguo. " +
                        "Nunca escolha arbitrariamente um candidato. " +
                        "Se a Tool retornar NOT_FOUND, informe que o item não foi resolvido. " +
                        "Nunca crie registros. Nunca calcule preço. Nunca calcule custos. Nunca calcule margem. " +
                        "Nunca chame Pricing Engine. " +
                        "Preencha 'unresolvedFields' com os campos que não puderam ser resolvidos com sucesso. " +
                        "A flag 'fullyResolved' deve ser true SOMENTE se todos os campos principais solicitados pelo cliente foram resolvidos e não há ambiguidades. "
                        +
                        "O campo interpretation deve conter a interpretação original sem IDs (mesmas regras da interpretação simples).";

        public CatalogResolveAgent(
                        @Value("${printpilot.ai.model:gemini-3.5-flash-lite}") String modelName,
                        CatalogTools catalogTools) {

                this.objectMapper = new ObjectMapper();

                GenerateContentConfig config = GenerateContentConfig.builder()
                                .responseMimeType("application/json")
                                .temperature(0.0f)
                                .build();

                FunctionTool resolveProductTool = FunctionTool.create(catalogTools, "resolveProduct");
                FunctionTool resolveMaterialTool = FunctionTool.create(catalogTools, "resolveMaterial");
                FunctionTool resolveFinishingTool = FunctionTool.create(catalogTools, "resolveFinishing");

                Schema finishingInterpretationSchema = Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "name", Schema.builder().type(Type.Known.STRING).build(),
                                                "quantity", Schema.builder().type(Type.Known.INTEGER).build()))
                                .build();

                Schema interpretationSchema = Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "product", Schema.builder().type(Type.Known.STRING).build(),
                                                "pricingType", Schema.builder().type(Type.Known.STRING).build(),
                                                "quantity", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "width", Schema.builder().type(Type.Known.NUMBER).build(),
                                                "height", Schema.builder().type(Type.Known.NUMBER).build(),
                                                "material", Schema.builder().type(Type.Known.STRING).build(),
                                                "finishings",
                                                Schema.builder().type(Type.Known.ARRAY)
                                                                .items(finishingInterpretationSchema).build(),
                                                "missingFields",
                                                Schema.builder().type(Type.Known.ARRAY)
                                                                .items(Schema.builder().type(Type.Known.STRING).build())
                                                                .build()))
                                .build();

                Schema resolvedProductSchema = Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "status", Schema.builder().type(Type.Known.STRING).build(),
                                                "id", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "name", Schema.builder().type(Type.Known.STRING).build()))
                                .build();

                Schema resolvedMaterialSchema = Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "status", Schema.builder().type(Type.Known.STRING).build(),
                                                "id", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "name", Schema.builder().type(Type.Known.STRING).build()))
                                .build();

                Schema resolvedFinishingSchema = Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "requestedName", Schema.builder().type(Type.Known.STRING).build(),
                                                "requestedQuantity", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "status", Schema.builder().type(Type.Known.STRING).build(),
                                                "id", Schema.builder().type(Type.Known.INTEGER).build(),
                                                "name", Schema.builder().type(Type.Known.STRING).build()))
                                .build();

                Schema rootSchema = Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "interpretation", interpretationSchema,
                                                "product", resolvedProductSchema,
                                                "material", resolvedMaterialSchema,
                                                "finishings",
                                                Schema.builder().type(Type.Known.ARRAY).items(resolvedFinishingSchema)
                                                                .build(),
                                                "fullyResolved", Schema.builder().type(Type.Known.BOOLEAN).build(),
                                                "unresolvedFields",
                                                Schema.builder().type(Type.Known.ARRAY)
                                                                .items(Schema.builder().type(Type.Known.STRING).build())
                                                                .build(),
                                                "ambiguities",
                                                Schema.builder().type(Type.Known.ARRAY)
                                                                .items(Schema.builder().type(Type.Known.STRING).build())
                                                                .build()))
                                .build();

                this.agent = LlmAgent.builder()
                                .name("catalog_resolver")
                                .description("Resolve pedidos utilizando o catálogo.")
                                .model(modelName)
                                .instruction(SYSTEM_PROMPT)
                                .generateContentConfig(config)
                                .tools(List.of(resolveProductTool, resolveMaterialTool, resolveFinishingTool))
                                .outputSchema(rootSchema)
                                .build();

                this.runner = new InMemoryRunner(agent, "printpilot-resolve");
        }

        public CatalogResolveResponse resolve(String text) {
                try {
                        Content content = Content.builder()
                                        .role("user")
                                        .parts(List.of(Part.builder().text(text).build()))
                                        .build();

                        String USER_ID = "api-user";
                        String SESSION_ID = UUID.randomUUID().toString();

                        Session session = runner.sessionService()
                                        .createSession(
                                                        runner.appName(),
                                                        USER_ID,
                                                        new java.util.HashMap<>(),
                                                        SESSION_ID)
                                        .blockingGet();

                        Event finalEvent = runner.runAsync(
                                        session.userId(),
                                        session.id(),
                                        content)
                                        .filter(Event::finalResponse)
                                        .blockingLast();

                        String jsonResponse = finalEvent.stringifyContent();

                        if (jsonResponse == null || jsonResponse.isBlank()) {
                                throw new IllegalStateException(
                                                "AI provider returned an empty final response");
                        }

                        return objectMapper.readValue(
                                        jsonResponse,
                                        CatalogResolveResponse.class);
                } catch (Exception e) {
                        throw new RuntimeException("Error communicating with AI Provider", e);
                }
        }
}
