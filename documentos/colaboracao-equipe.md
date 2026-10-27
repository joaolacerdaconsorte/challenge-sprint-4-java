# Evidências de Colaboração da Equipe — Turma 2TDSPW

> **Organização da Equipe 2TDSPW:** Divisão clara das responsabilidades técnicas, módulos arquiteturais e blocos de apresentação em vídeo entre os 4 integrantes do grupo.

## Integrantes e Responsabilidades Técnicas na Sprint 4

| Integrante | RM | Papel Principal na Sprint 4 | Módulos e Arquivos sob Liderança |
|---|---|---|---|
| **João Vitor Lacerda Consorte** | RM 565565 | Arquitetura Core, Segurança Spring Security 6, OAuth2 (GitHub/Google) e Internacionalização (`i18n`) | `SecurityConfig.java`, `CustomOAuth2UserService.java`, `LoginWebController.java`, `InternacionalizacaoConfig.java`, `messages_pt_BR.properties`, `messages_en.properties`, `templates/login.html` |
| **Kauan Vieira de Lima** | RM 565403 | Integração IoT & IA Híbrida (Copiloto Clínico SOAP, Cálculo de Score IRP e Telemetria ESP32) | `TelemetriaIot.java`, `TelemetriaIotRepository.java`, `CopilotoIaTelemetriaService.java`, `IaTelemetriaWebController.java`, `templates/ia/telemetria.html` |
| **Murillo Fernandes Carapia** | RM 564969 | Governança de Dados (Flyway V3, Motor JSON NoSQL `FN_PET_JSON`, Auditoria `TB_AUDITORIA_PET`) & DevOps Cloud | `V3__sprint4_oauth2_telemetria_auditoria.sql`, `AuditoriaPet.java`, `IntegracaoMultidisciplinarService.java`, `IntegracaoWebController.java`, `Dockerfile`, `render.yaml`, `templates/integracao/hub.html` |
| **Pedro de Matos Previtali** | RM 564184 | Motores Transacionais (Triagem e Atendimento Clínico), UI/UX Thymeleaf, Documentação ADR e Testes Automatizados QA | `TriagemAgendamentoService.java`, `AtendimentoClinicoService.java`, `PetService.java`, `DashboardWebController.java`, `PetCare360Sprint3ApplicationTests.java`, `documentos/adr/*` |

## Fluxo de Trabalho Colaborativo
1. **Planejamento e Revisão do Feedback da Sprint 3:** Análise conjunta do feedback do Prof. Leonardo Real (inclusão prioritária de OAuth2 Google/GitHub na página de login) e dos critérios da Sprint 4 (Deploy em nuvem, ADRs, Integração Multidisciplinar e Vídeo de até 15 minutos).
2. **Desenvolvimento Modular e Integração Contínua:** Centralização da integração no repositório oficial com commits semânticos (*Conventional Commits*: `chore:`, `feat:`, `test:`, `docs:`) e divisão de módulos de domínio por integrante.
3. **Revisão Cruzada e Qualidade (QA):** Execução obrigatória da suíte `./gradlew test` (11 testes automatizados JUnit 5 + MockMvc passando 100%) antes da publicação da imagem Docker em nuvem.
