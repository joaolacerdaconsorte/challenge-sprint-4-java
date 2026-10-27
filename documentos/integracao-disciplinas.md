# Matriz de Integração Multidisciplinar — PetCare 360 (CLYVO VET 2026)

> **Critério Oficial FIAP — 4ª Sprint (20 pontos):** *Demonstração clara e explícita de como os conhecimentos das demais disciplinas foram aplicados na construção da solução, evidenciando coerência e complementaridade entre as áreas.*

A arquitetura do **PetCare 360 (CLYVO VET)** na **Sprint 4 de Java Advanced** foi desenhada como o **núcleo integrador** de todas as disciplinas cursadas pela equipe na turma **2TDSPW**:

---

## 1. Mastering Relational and Non-Relational Database (Nota Sprint 3: 95/100)
- **O que foi desenvolvido em Database:**
  - Modelagem relacional na **3ª Forma Normal (3FN)** (`TB_TUTOR`, `TB_CLINICA`, `TB_PET`, `TB_CONSULTA`, `TB_VACINA`, `TB_MEDICAMENTO`, `TB_AGENDAMENTO`, `TB_ALERTA`).
  - Pacote Oracle PL/SQL `PKG_CLYVO_VET` contendo as funções `FN_PET_JSON` (conversão de dados relacionais para documento JSON), `FN_CALC_RISCO_VACINAL` (classificação de imunização) e a trigger `TRG_AUDITORIA_PET` (gravação automática em `TB_AUDITORIA_PET`).
- **Como está integrado e vivo no Java Advanced (Sprint 4):**
  - Migrations Flyway (`V1`, `V2`, `V3`) mantendo o schema 3FN + `TB_TELEMETRIA_IOT` + `TB_AUDITORIA_PET`.
  - O serviço [`IntegracaoMultidisciplinarService`](../src/main/java/br/com/fiap/pet360/service/IntegracaoMultidisciplinarService.java) implementa em Java a mesma lógica de `FN_PET_JSON` e `FN_CALC_RISCO_VACINAL`, disponível tanto na tela web **Hub Multidisciplinar (`/integracao`)** quanto via API REST (`GET /api/integracao/pets/{id}/json`) para ingestão no **MongoDB / Azure Cosmos DB**.
  - Os serviços [`PetService`](../src/main/java/br/com/fiap/pet360/service/PetService.java) e [`AtendimentoClinicoService`](../src/main/java/br/com/fiap/pet360/service/AtendimentoClinicoService.java) espelham a trigger `TRG_AUDITORIA_PET`, gravando automaticamente cada `INSERT`, `UPDATE` (incluindo atualização de peso na consulta) e `DELETE` na tabela `TB_AUDITORIA_PET`.

---

## 2. Disruptive Architectures: IoT, IoB & Generative AI
- **O que foi desenvolvido em IoT & IA:**
  - Coleira inteligente simulada no **Wokwi (ESP32 + Sensor DHT22 + Acelerômetro IOB)** publicando telemetria via **MQTT** (`https://wokwi.com/projects/464135788106122241`).
  - Modelo preditivo **Random Forest (acurácia 93,4%)** para cálculo do **Índice de Risco Preventivo (IRP 0–100)** e **Copiloto Clínico IA** gerando notas clínicas no padrão **SOAP (Subjetivo, Objetivo, Avaliação, Plano)** e orientações empáticas ao tutor.
- **Como está integrado e vivo no Java Advanced (Sprint 4):**
  - Entidade [`TelemetriaIot`](../src/main/java/br/com/fiap/pet360/model/TelemetriaIot.java) (`TB_TELEMETRIA_IOT`) e motor transacional [`CopilotoIaTelemetriaService`](../src/main/java/br/com/fiap/pet360/service/CopilotoIaTelemetriaService.java).
  - Na tela **Copiloto IA & IoT (`/ia-telemetria`)** e no endpoint `POST /api/telemetria`, o sistema recebe os sinais vitais do ESP32 (`temperaturaCorporal`, `umidadeAmbiente`, `atividadeIob`, `batimentosBpm`), cruza com o histórico vacinal e medicamentoso do pet no banco relacional, calcula o **Score IRP (0–100)**, gera o prontuário **SOAP** completo e dispara automaticamente um `Alerta` crítico caso detecte hipertermia (`> 39.4 °C`) ou risco `ALTO`.

---

## 3. Mobile Application Development (Nota Sprint 3: 100/100)
- **O que foi desenvolvido em Mobile:**
  - Aplicativo **React Native + Expo + TypeScript** para tutores e clínicas com autenticação, carteira digital de vacinação, agenda e alertas preventivos (`https://github.com/FIAP-MOBILE-2026-Fevereiro/2tdspw-challenge-joaorm565565-murillo564969-kauan.git`).
- **Como está integrado e vivo no Java Advanced (Sprint 4):**
  - A camada **API RESTful (`/api/**` e `/auth/login`)** do Java Advanced fornece autenticação **JWT (`Bearer`)**, paginação e **HATEOAS (Nível 3 de Richardson)** para consumo direto pelo aplicativo mobile.

---

## 4. DevOps Tools & Cloud Computing + Advanced Business Development with .NET
- **O que foi desenvolvido em DevOps & Cloud:**
  - Pipeline CI/CD no **Azure DevOps** (`https://dev.azure.com/RM564969/Sprint%204%20-%20Azure%20DevOps`), containerização Docker segura (usuário non-root) e deploy em nuvem no **Azure Web App for Containers** conectado ao Oracle FIAP e ao Azure Cosmos DB (MongoDB API), com health check ativo em `https://petcare360-rm564969.azurewebsites.net/health`.
- **Como está integrado e vivo no Java Advanced (Sprint 4):**
  - `Dockerfile` Multi-Stage com JDK/JRE 21 Alpine e usuário `appuser` non-root, `render.yaml` e endpoints `/health` e `/actuator/health`, além de link direto de verificação de saúde da nuvem Azure dentro do **Hub Multidisciplinar (`/integracao`)**.

---

## 5. Quality Assurance (QA), Compliance & Tests
- **O que foi desenvolvido em QA:**
  - Governança ágil no **Azure Boards** (`https://dev.azure.com/fiap-challenge-2026/PetCare360/_boards/plan/`), arquitetura TOGAF/ArchiMate, 4 Épicos e **11 PBIs escritos em BDD (Gherkin: Dado / Quando / Então)**.
- **Como está integrado e vivo no Java Advanced (Sprint 4):**
  - Suíte automatizada [`PetCare360Sprint3ApplicationTests`](../src/test/java/br/com/fiap/pet360/PetCare360Sprint3ApplicationTests.java) com **11 testes de integração JUnit 5 + MockMvc** validando 100% dos critérios de aceite BDD: migrations Flyway, redirecionamento de segurança, login OAuth2 (GitHub/Google), internacionalização (`PT-BR`/`EN`), RBAC (`403 Forbidden`), Triagem Inteligente, Atendimento Clínico + Auditoria, Copiloto IA/IoT SOAP, exportação JSON NoSQL e Health Check.
