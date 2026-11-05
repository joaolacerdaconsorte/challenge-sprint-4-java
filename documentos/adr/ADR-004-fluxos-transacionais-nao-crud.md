# ADR-004: Motores Transacionais Não-CRUD (Triagem, Atendimento Clínico, Copiloto IA/IoT e Hub NoSQL)

- **Status:** Aceito
- **Data:** Outubro de 2026
- **Decisores:** Equipe PetCare 360 — Turma 2TDSPW

## Contexto e Problema
O problema proposto pela **CLYVO 2026** exige que o sistema vá muito além de cadastros básicos (CRUD), atuando ativamente na **medicina veterinária preventiva**, evitando esquecimento de reforços vacinais, choques medicamentosos (alergias) e atrasos na detecção de quadros febris/letárgicos.

## Decisão
Projetamos **4 Motores de Negócio Transacionais Não-CRUD (`@Transactional`)**:
1. **`TriagemAgendamentoService` (Fluxo 1):** Ao agendar um atendimento, analisa automaticamente o histórico do Pet (alergias críticas como dipirona, vacinas vencidas ou a vencer em 15 dias, medicamentos ativos e idade sênior >= 7 anos) e gera alertas preventivos automáticos em `TB_ALERTA`.
2. **`AtendimentoClinicoService` (Fluxo 2):** Em uma única transação atômica acionada pelo veterinário, atualiza o peso do Pet, grava auditoria em `TB_AUDITORIA_PET`, registra a `Consulta`, aplica `Vacina` com cálculo automático da próxima dose (+365 dias), prescreve `Medicamento` e encerra o `Agendamento`.
3. **`CopilotoIaTelemetriaService` (Fluxo 3 — Integração IoT & IA):** Processa pacotes fisiológicos da coleira inteligente ESP32 (temperatura, umidade, atividade IOB e batimentos cardíacos), calcula o **Índice de Risco Preventivo (IRP 0–100)**, gera o prontuário estruturado **SOAP (`[S]`, `[O]`, `[A]`, `[P]`)** e a recomendação empática ao tutor, disparando alerta crítico caso detecte hipertermia (`> 39.4 °C`) ou risco `ALTO` (`IRP >= 70`).
4. **`IntegracaoMultidisciplinarService` (Fluxo 4 — Equivalente PL/SQL & NoSQL):** Executa em Java a lógica equivalente às funções Oracle `FN_CALC_RISCO_VACINAL` e `FN_PET_JSON`, exportando documentos JSON prontos para o **MongoDB / Azure Cosmos DB** e mantendo a trilha de auditoria (`TRG_AUDITORIA_PET`).

## Justificativa e Consequências
- **Integridade Atômica (ACID):** Caso qualquer etapa falhe durante o atendimento clínico ou processamento de telemetria, o rollback transacional garante zero dados órfãos.
- **Alinhamento Total ao Problema CLYVO:** Transforma dados brutos em inteligência preventiva acionável.
