# ADR-002: Segurança Unificada com Spring Security 6, RBAC, OAuth2 (GitHub & Google) e JWT

- **Status:** Aceito (Evoluído na Sprint 4 em resposta ao Feedback Oficial da Sprint 3)
- **Data:** Outubro de 2026
- **Decisores:** Equipe PetCare 360 — Turma 2TDSPW

## Contexto e Problema
Na Sprint 3, implementamos autenticação stateful com `DaoAuthenticationProvider` (BCrypt), `formLogin` customizado e controle de acesso baseado em papéis (`ADMIN`, `VETERINARIO`, `USER`), além de JWT para a API REST.
O feedback oficial do Prof. Leonardo Real (`lgsreal`) na Sprint 3 apontou especificamente:
> *"O que faltou para nota máxima? Adicionar OAuth2 com login de terceiros (Google e/ou GitHub) à página de login."*

## Decisão
Evoluímos a `SecurityConfig` e a camada de identidade na Sprint 4 com:
1. **Integração OAuth2 Client (`spring-boot-starter-oauth2-client`):** Configuração oficial dos provedores **GitHub** (`/oauth2/authorization/github`) e **Google** (`/oauth2/authorization/google`) no `application.yml` e no `SecurityFilterChain` via `.oauth2Login(...)`.
2. **Provisionamento Federado Automático (`CustomOAuth2UserService`):** Ao autenticar via GitHub ou Google, o serviço sincroniza automaticamente o usuário federado na tabela `TB_USUARIO` (colunas `OAUTH_PROVIDER` e `EMAIL` adicionadas na migration `V3`), atribuindo o perfil `ROLE_USER` (Tutor) ou preservando o papel administrativo/clínico já existente.
3. **RBAC Granular + Thymeleaf Security Extras:** Segregação estrita de rotas e elementos visuais (`sec:authorize`) entre `ADMIN` (gestão de clínicas e exclusões), `VETERINARIO` (fila de atendimento clínico) e `USER` (portal do tutor, triagem e telemetria).

## Justificativa e Consequências
- **Eliminação de Fricção para Tutores:** Tutores podem acessar a plataforma PetCare 360 com 1 clique usando suas contas Google ou GitHub, sem necessidade de memorizar senhas adicionais.
- **Atendimento Integral ao Feedback da Banca:** Resolve 100% do apontamento da Sprint 3 mantendo compatibilidade com o login tradicional (`DaoAuthenticationProvider`) e com tokens JWT na API REST.
