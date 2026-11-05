# ADR-006: Containerização Multi-Stage Non-Root e Deploy em Nuvem (Render / Azure / Docker)

- **Status:** Aceito (Novo na Sprint 4)
- **Data:** Outubro de 2026
- **Decisores:** Equipe PetCare 360 — Turma 2TDSPW

## Contexto e Problema
A 4ª Sprint exige que a solução esteja rodando **online (deploy)** com boas práticas de segurança DevSecOps e observabilidade de saúde da aplicação, alinhada à entrega de **DevOps Tools & Cloud Computing**.

## Decisão
Implementamos:
1. **`Dockerfile` Multi-Stage:**
   - *Stage 1 (`builder`):* `eclipse-temurin:21-jdk-alpine` compilando o fat JAR via `./gradlew bootJar -x test`.
   - *Stage 2 (`runtime`):* `eclipse-temurin:21-jre-alpine` executando sob usuário **não-root** (`appuser:petcare`), com `MaxRAMPercentage=75.0` e `HEALTHCHECK` nativo.
2. **Observabilidade (`/health` e `/actuator/health`):** Endpoints públicos de verificação de integridade para balanceadores de carga na nuvem (Render Blueprint `render.yaml` e Azure App Service).
3. **Configuração Cloud-Native (`${PORT:8080}` e `forward-headers-strategy: framework`):** Garante funcionamento transparente atrás de proxies reversos HTTPS em nuvem (para que redirecionamentos OAuth2 usem `https://` corretamente).

## Justificativa e Consequências
- **Segurança em Produção:** Impede execução com privilégios `root` dentro do container e reduz a superfície de ataque usando imagem JRE Alpine enxuta.
- **Portabilidade Total:** Roda de forma idêntica via `docker compose up --build`, Render Cloud ou Azure Web App for Containers.
