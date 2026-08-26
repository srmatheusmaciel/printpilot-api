# PrintPilot AI

Plataforma SaaS voltada para a geração e gerenciamento de orçamentos inteligentes para gráficas.

---

## Visão Geral

O processo de criação de orçamentos em gráficas frequentemente envolve muitos cálculos de área, perdas de materiais, preços de acabamentos e taxas manuais de mão de obra. O **PrintPilot AI** tem o objetivo de estruturar e automatizar esse processo, substituindo a complexidade por um fluxo otimizado:

```text
Pedido
  ↓
Dados estruturados
  ↓
Customer
  ↓
Quote (Produto + Material + Acabamentos)
  ↓
Pricing Engine
  ↓
Composição de custos (Material + Impressão + Acabamento + Desperdício + Mão de obra)
  ↓
Preço sugerido e Persistência
  ↓
Orçamento
```

### Arquitetura Futura (IA)
Em futuras versões, a entrada de dados (pedido) será gerada a partir da interpretação de linguagem natural pela IA:

```text
IA interpreta pedido → Backend (Pricing Engine) calcula os valores
```

> **Atenção:** Modelos de IA não são responsáveis pelos cálculos financeiros. Este é um princípio arquitetural do PrintPilot. A IA atua **apenas na interpretação do pedido**, deixando os cálculos estritamente para o motor de orçamentos determinístico implementado no backend (Java).

---

## Estado Atual

✅ Cadastro de materiais  
✅ Cadastro de produtos  
✅ Cadastro de acabamentos  
✅ Regras de precificação por área  
✅ Cálculo de orçamento por área  
✅ Persistência de orçamentos  
✅ Gerenciamento de clientes (Customer)  
✅ Histórico de orçamentos por cliente  
✅ Pricing Engine AREA
✅ Pricing Engine QUANTITY

🚧 **Em desenvolvimento / Planejado:**
- Interpretador IA de pedidos
- Autenticação
- Exportação de orçamento em PDF
- Gestão de clientes
- Gestão de estoque

---

## Principais Funcionalidades

### Materials
Cadastro dos insumos utilizados na gráfica, com especificação da unidade de medida (ex.: m², unidades) e custo base.

### Products
Cadastro dos produtos que a gráfica vende. Os produtos ditam a estratégia de precificação:
* `PricingType.AREA` (ex.: Banners, Adesivos)
* `PricingType.QUANTITY` (ex.: Cartão de visita, Panfleto)

### Finishings
Cadastro de acabamentos opcionais com modelos de custo flexíveis (por unidade, por área ou fixo). Ex.: Ilhós, Laminação, Verniz.

### Area Pricing Rules
Configurações de custo (impressão, mão de obra, percentuais de margem e desperdício) anexadas a um produto que usa precificação por área.

### Pricing Engine
Motor determinístico responsável por compilar todas as regras e insumos, resultando em um breakdown preciso de todos os custos até chegar no preço sugerido.

### Quotes
Módulo que processa simulações através do Pricing Engine e cria versões permanentes do orçamento (snapshots). Isso garante que mudanças futuras nos custos não alterem o histórico de orçamentos aprovados. Um Quote pode ser associado opcionalmente a um Customer.

---

## Fórmulas do Pricing Engine (PricingType.AREA)

A etapa inicial foi construída para dar suporte à precificação por `AREA`.
O breakdown financeiro acontece da seguinte forma:

- **unitArea** = `width × height`
- **totalArea** = `width × height × quantity`
- **materialCost** = `totalArea × materialCostPerSquareMeter`
- **printingCost** = `totalArea × printingCostPerSquareMeter`

**Acabamentos (`FinishingPricingType`)**:
- *UNIT*: `cost × quantity_informada`
- *AREA*: `cost × totalArea`
- *FIXED*: `cost`

**Cálculos Finais**:
- **wasteCost** = `(materialCost + printingCost) × (wastePercentage / 100)`
- **totalCost** = `materialCost + printingCost + finishingCost + wasteCost + laborCost`
- **suggestedPrice** = `totalCost / (1 - marginPercentage / 100)`

---

## Fórmulas do Pricing Engine (PricingType.QUANTITY)

Para produtos calculados por quantidade (ex: Cartão de visita, Panfleto), utilizamos folhas inteiras:

- **requiredSheets** = `ArredondaParaCima( quantity / unitsPerSheet )`
- **materialCost** = `requiredSheets × material.cost`
- **printingCost** = `quantity × printingCostPerUnit`

**Acabamentos (`FinishingPricingType`)**:
- *UNIT*: `cost × quantity_informada`
- *FIXED*: `cost`
*(Acabamentos por AREA não são suportados em orçamentos QUANTITY)*

**Cálculos Finais**:
- **wasteCost** = `(materialCost + printingCost) × (wastePercentage / 100)`
- **totalCost** = `materialCost + printingCost + finishingCost + wasteCost + laborCost`
- **suggestedPrice** = `totalCost / (1 - marginPercentage / 100)`

---

## Tecnologias

**Backend:**
- Java 21
- Spring Boot (Spring Web, Spring Data JPA, Bean Validation)
- Lombok
- Flyway (Migrações)
- Springdoc OpenAPI (Swagger UI)

**Banco de Dados:**
- PostgreSQL 16

**Infraestrutura:**
- Docker / Docker Compose

**Build:**
- Maven

---

## Arquitetura do Projeto

O código-fonte (`src/main/java/br/com/printpilot`) está organizado por **responsabilidade**, não devendo seguir padrões como package-by-feature, CQRS ou Hexagonal neste momento.

```text
├── config      → Configurações técnicas (ex.: OpenAPI)
├── controller  → Endpoints REST
├── dto         → Contratos de entrada e saída (Request/Response)
├── entity      → Entidades JPA e mapeamento ORM
├── enums       → Tipos enumerados de domínio
├── repository  → Acesso e abstração de banco de dados
└── service     → Regras de negócio, cálculos e integrações
```

---

## Banco de Dados e Migrations

As estruturas das tabelas não são geradas pelo Hibernate (`spring.jpa.hibernate.ddl-auto=none`), mas sim controladas explicitamente pelo **Flyway**.

Migrations atuais do projeto:
- `V1__create_materials_table.sql`
- `V2__create_products_table.sql`
- `V3__create_finishings_table.sql`
- `V4__create_area_pricing_rules_table.sql`
- `V5__create_quotes_table.sql`
- `V6__create_customers_and_link_quotes.sql`

---

## Executando Localmente

### Pré-requisitos
- Java 21
- Docker e Docker Compose
- Git
- Maven (Opcional caso não deseje usar o `./mvnw` incluído no wrapper)

### Passos

1. **Clone do repositório**
   ```bash
   git clone https://github.com/srmatheusmaciel/printpilot-api.git
   cd printpilot-api
   ```

2. **Configuração (.env)**
   Crie um arquivo `.env` na raiz do projeto (nunca versione este arquivo). Exemplo:
   ```env
   POSTGRES_DB=printpilot
   POSTGRES_USER=printpilot
   POSTGRES_PASSWORD=your_password
   POSTGRES_PORT=5434
   ```

3. **Iniciando o banco de dados via Docker**
   ```bash
   docker compose up -d
   ```

4. **Rodando a Aplicação**
   Exporte as variáveis de ambiente e rode a aplicação via Maven Wrapper:
   ```bash
   set -a
   source .env
   set +a
   
   ./mvnw spring-boot:run
   ```

5. **Executando Testes**
   ```bash
   ./mvnw clean test
   ```

*(Opcional)* Acesso direto ao PostgreSQL via Docker:
```bash
docker exec -it printpilot-db psql -U printpilot -d printpilot
```

---

## URLs Úteis

Com o projeto em execução localmente, os seguintes endereços estarão disponíveis:

- **API Base URL:** `http://localhost:8080`
- **Health Check:** `http://localhost:8080/api/health`
- **Swagger UI (Documentação e Testes):** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Roadmap e Planejamento

- [x] Materials
- [x] Products
- [x] Finishings
- [x] Area Pricing Rules
- [x] AREA Pricing Engine
- [x] Quote persistence
- [x] Customer management
- [x] Customer quote history
- [x] QUANTITY Pricing Engine
- [ ] IA para interpretação de pedidos
- [ ] PDF de orçamento
- [ ] Autenticação e Autorização (Spring Security)
- [ ] Frontend integration

---

## Segurança

O arquivo `.env` está explicitamente ignorado via `.gitignore`.
**NUNCA** adicione ou commite senhas de banco de dados, chaves de API, Tokens JWT ou segredos na base de código. 
