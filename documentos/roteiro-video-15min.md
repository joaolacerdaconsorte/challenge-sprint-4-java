# Roteiro Oficial de Apresentação em Vídeo (Até 15 Minutos) — Sprint 4 Java Advanced

> **Regra Oficial FIAP (Slide de Avaliação):**
> - Duração máxima: **15 minutos**.
> - **Todos os 4 integrantes devem participar** com fala clara, objetiva e domínio técnico.
> - A demonstração deve exibir a aplicação rodando **online (deploy)**, navegando pelos fluxos principais, explicando as decisões arquiteturais (Narrativa) e evidenciando a **Integração Multidisciplinar**.

---

## Divisão de Tempo Sugerida (Total: ~12 a 14 minutos)

### Bloco 1 — João Vitor Lacerda Consorte (RM 565565) | `00:00 – 03:30`
**Tema:** *Proposta de Valor CLYVO VET, Evolução Pós-Feedback (OAuth2 Google/GitHub), Segurança RBAC e Internacionalização (`i18n`)*
1. **Abertura (30s):** Apresentar a equipe 2TDSPW e o propósito do **PetCare 360 (CLYVO VET)**: transformar o cuidado veterinário reativo em medicina preventiva orientada por dados, IoT e Inteligência Artificial.
2. **Demonstração na URL Pública em Nuvem (1m30s):**
   - Mostrar a aplicação rodando online na URL pública e o endpoint `/health` respondendo `200 OK {"status":"Healthy"}`.
   - Na tela `/login`, destacar a correção exata do feedback da Sprint 3: os botões **Continuar com GitHub** (`/oauth2/authorization/github`) e **Continuar com Google** (`/oauth2/authorization/google`), além do **Modo Avaliador OAuth2 Sandbox** que provisiona o usuário federado via `CustomOAuth2UserService` em `TB_USUARIO`.
   - Clicar no seletor de idiomas **🇧🇷 PT-BR | 🇺🇸 EN** (`?lang=en` e `?lang=pt_BR`) demonstrando a internacionalização nativa (`SessionLocaleResolver` + `ResourceBundleMessageSource`).
3. **Demonstração de Segurança RBAC (1m30s):**
   - Fazer login com `tutor` / `tutor123` (`ROLE_USER`) ou via OAuth2, mostrar o Dashboard e tentar acessar `/clinicas/nova`, exibindo a página customizada de **Acesso Negado (403 Forbidden)**.

---

### Bloco 2 — Pedro de Matos Previtali (RM 564184) | `03:30 – 07:00`
**Tema:** *Narrativa Arquitetural (ADRs), Flyway e Fluxos Transacionais Não-CRUD 1 e 2 (Triagem & Atendimento Clínico)*
1. **Decisões de Arquitetura e Governança Flyway (1m):**
   - Explicar brevemente o uso de `ddl-auto: validate` com as migrations `V1`, `V2` e `V3` e os 6 ADRs documentados no repositório.
2. **Fluxo Não-CRUD 1 — Triagem e Agendamento Inteligente (`/agendamentos/novo`) (1m):**
   - Selecionar o pet **Thor** (Bulldog Francês com vacina Antirrábica vencida e histórico de displasia), agendar uma consulta e mostrar que o `TriagemAgendamentoService` bloqueia conflitos de horário e gera automaticamente um **Alerta Preventivo** de vacina vencida!
3. **Fluxo Não-CRUD 2 — Atendimento Clínico Integrado (`/atendimento/fila`) (1m30s):**
   - Logar como `veterinario` / `vet123` (`ROLE_VETERINARIO`), abrir a Fila Clínica e clicar em **Atender** no agendamento do **Rex**.
   - Atualizar o peso do Rex, registrar diagnóstico, aplicar vacina (com cálculo automático de +365 dias) e prescrever medicamento em uma única transação `@Transactional`, mostrando o histórico unificado em `/pets/{id}`.

---

### Bloco 3 — Kauan Vieira de Lima (RM 565403) | `07:00 – 10:30`
**Tema:** *Integração com Disruptive Architectures (IoT ESP32 Wokwi, MQTT, Score IRP e Copiloto Clínico IA SOAP)*
1. **Conexão Hardware IoT → Java Advanced (1m):**
   - Mostrar rapidamente o projeto da coleira inteligente no simulador **Wokwi ESP32** (sensor de temperatura/umidade DHT22 + acelerômetro de atividade IOB) e abrir a tela **Copiloto IA & IoT (`/ia-telemetria`)** no sistema Java.
2. **Fluxo Não-CRUD 3 — Processamento de Telemetria e Prontuário SOAP (2m30s):**
   - Simular o envio de um pacote MQTT de hipertermia e letargia para o **Thor** (ex.: Temperatura `39.9 °C`, Umidade `70%`, Atividade IOB `25%`, `145 BPM`).
   - Mostrar como o `CopilotoIaTelemetriaService` cruza a telemetria com as vacinas vencidas e medicamentos ativos no banco, calcula o **Score IRP (Índice de Risco Preventivo = ALTO)**, gera automaticamente a nota clínica estruturada **SOAP (`[S]`, `[O]`, `[A]`, `[P]`)** para o veterinário, a **Orientação Empática ao Tutor** e dispara um alerta automático na central `/alertas`!

---

### Bloco 4 — Murillo Fernandes Carapia (RM 564969) | `10:30 – 14:00`
**Tema:** *Hub de Integração Multidisciplinar (Database PL/SQL JSON, Trigger de Auditoria, DevOps Cloud Azure, Mobile & QA)*
1. **Fluxo Não-CRUD 4 — Hub Multidisciplinar (`/integracao`) (2m):**
   - Demonstrar na tela `/integracao` o motor híbrido que replica as funções Oracle PL/SQL da disciplina de **Database (Nota 95/100)**: `FN_CALC_RISCO_VACINAL` e `FN_PET_JSON`, gerando em tempo real o documento JSON completo do pet (e abrindo `/api/integracao/pets/{id}/json`) pronto para o **MongoDB / Azure Cosmos DB**.
   - Mostrar ao lado a tabela de **Trilha de Auditoria Clínica (`TB_AUDITORIA_PET`)**, comprovando que o atendimento clínico realizado minutos antes pelo Pedro gravou automaticamente os valores antigos, valores novos e o usuário responsável (espelhando a trigger `TRG_AUDITORIA_PET`).
2. **Convergência Cloud DevOps, Mobile (Nota 100/100) e QA BDD (1m30s):**
   - Mostrar o link vivo do **Azure App Service** (`https://petcare360-rm564969.azurewebsites.net/health`), o `Dockerfile` multi-stage non-root, o Swagger (`/swagger-ui.html`) consumido pelo app **Mobile React Native** e a execução dos **11 testes automatizados JUnit 5 + MockMvc (`BUILD SUCCESSFUL`)** alinhados aos cenários BDD de **QA**.
