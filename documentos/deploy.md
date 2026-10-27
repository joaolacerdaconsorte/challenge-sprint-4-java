# Guia de Deploy em Nuvem e Execução — PetCare 360 (Sprint 4)

## 1. Links Oficiais de Produção (Deploy Online)
- **Aplicação Web & API Java Advanced (Sprint 4):** Disponível publicamente (veja link atualizado no topo do `README.md`).
- **Health Check Java Advanced:** `/health` e `/actuator/health`
- **Swagger UI (OpenAPI 3):** `/swagger-ui.html`
- **Ecossistema Cloud Integrado (Azure App Service — DevOps & .NET):** `https://petcare360-rm564969.azurewebsites.net/health`
- **Simulador Hardware IoT (Wokwi ESP32):** `https://wokwi.com/projects/464135788106122241`

---

## 2. Como Executar via Docker (Multi-Stage Non-Root)
```bash
docker compose up --build -d
```
Acesse: `http://localhost:8080/login`

---

## 3. Como Executar Localmente com Gradle (JDK 21)
```bash
# Linux / macOS
./gradlew bootRun

# Windows (PowerShell / CMD)
.\gradlew.bat bootRun
```
Acesse: `http://localhost:8080/login`

---

## 4. Variáveis de Ambiente Suportadas em Nuvem (Render / Azure / Railway)
| Variável | Descrição | Valor Padrão (Fallback Seguro) |
|---|---|---|
| `PORT` | Porta HTTP atribuída pelo provedor cloud | `8080` |
| `GITHUB_CLIENT_ID` | Client ID do OAuth App no GitHub | `petcare360-github-client-id` |
| `GITHUB_CLIENT_SECRET` | Client Secret do OAuth App no GitHub | `petcare360-github-secret` |
| `GOOGLE_CLIENT_ID` | Client ID OAuth2 no Google Cloud Console | `petcare360-google-client-id` |
| `GOOGLE_CLIENT_SECRET` | Client Secret OAuth2 no Google Cloud Console | `petcare360-google-secret` |
| `JAVA_OPTS` | Flags de otimização de memória da JVM no container | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` |
