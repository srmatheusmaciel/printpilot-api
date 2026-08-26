package br.com.printpilot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI printPilotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PrintPilot AI API")
                        .version("v1")
                        .description("API REST para gerenciamento e geração de orçamentos inteligentes para gráficas.\n\n"
                                + "O PrintPilot gerencia:\n"
                                + "- Produtos\n"
                                + "- Materiais\n"
                                + "- Acabamentos\n"
                                + "- Regras de precificação\n"
                                + "- Cálculo de orçamento\n"
                                + "- Persistência de orçamentos\n\n"
                                + "Nota: A IA de interpretação ainda não está implementada nesta versão."));
    }
}
