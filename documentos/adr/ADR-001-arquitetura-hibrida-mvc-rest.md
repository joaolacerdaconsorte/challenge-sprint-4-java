# ADR-001: Arquitetura Híbrida Spring MVC (Thymeleaf SSR) + API RESTful (HATEOAS & OpenAPI)

- **Status:** Aceito (Sprint 3 & Evoluído na Sprint 4)
- **Data:** Outubro de 2026
- **Decisores:** Equipe PetCare 360 — Turma 2TDSPW

## Contexto e Problema
O desafio **CLYVO 2026** exige que a plataforma veterinária preventiva **PetCare 360** atenda simultaneamente a dois perfis distintos de consumo:
1. **Portal Web Clínico e Administrativo** (utilizado por recepcionistas, médicos veterinários e administradores nas clínicas parceiras, além de tutores via navegador desktop/mobile).
2. **Ecossistema Distribuído Multidisciplinar** (consumido pelo aplicativo **Mobile React Native/Expo**, pelos dispositivos **IoT ESP32** com telemetria fisiológica e pela camada analítica **NoSQL MongoDB/Azure Cosmos DB**).

## Decisão
Adotamos uma **Arquitetura Monolítica Modular Híbrida** sobre **Java 21 LTS** e **Spring Boot 3.3.5**, compartilhando a mesma camada de Domínio (`model`), Persistência (`repository`) e Regras de Negócio Transacionais (`service`), expondo duas interfaces desacopladas na camada de apresentação:
- **Camada Web SSR (`/dashboard`, `/pets`, `/agendamentos`, `/atendimento`, `/ia-telemetria`, `/integracao`):** Implementada com **Spring MVC + Thymeleaf + Bootstrap 5.3 + Internacionalização (`i18n` PT-BR/EN)**.
- **Camada API RESTful (`/api/**`, `/auth/**`, `/health`):** Implementada com **Spring HATEOAS (Nível 3 do Modelo de Maturidade de Richardson)**, **Bean Validation**, **Paginação** e documentação interativa via **SpringDoc OpenAPI 3 / Swagger UI**.

## Justificativa e Consequências
- **Reuso de Regras de Negócio (DRY & SOLID):** Os 4 motores transacionais não-CRUD (`TriagemAgendamentoService`, `AtendimentoClinicoService`, `CopilotoIaTelemetriaService` e `IntegracaoMultidisciplinarService`) são únicos e testados de ponta a ponta sem duplicação de código.
- **Baixa Latência e Simplicidade Operacional:** Um único artefato containerizado (`Dockerfile` multi-stage) serve tanto a interface web completa para a banca avaliadora quanto os endpoints REST para os demais módulos do semestre.
