# ADR-003: Governança de Schema e Evolução de Banco de Dados com Flyway (V1, V2 e V3)

- **Status:** Aceito
- **Data:** Outubro de 2026
- **Decisores:** Equipe PetCare 360 — Turma 2TDSPW

## Contexto e Problema
Aplicações corporativas que utilizam `ddl-auto: update` ou `create-drop` sofrem com inconsistências entre ambientes de desenvolvimento, testes automatizados e produção em nuvem. Além disso, a disciplina **Mastering Relational and Non-Relational Database (Nota 95/100)** estabeleceu um modelo relacional rigoroso na **3ª Forma Normal (3FN)** com auditoria automática (`TB_AUDITORIA_PET`).

## Decisão
Adotamos o **Flyway** como motor único de versionamento de banco de dados combinando `spring.jpa.hibernate.ddl-auto: validate`:
- **`V1__create_tables.sql`:** Criação das tabelas core (`TB_USUARIO`, `TB_TUTOR`, `TB_CLINICA`, `TB_PET`, `TB_CONSULTA`, `TB_VACINA`, `TB_MEDICAMENTO`, `TB_AGENDAMENTO`, `TB_ALERTA`) com chaves estrangeiras e índices otimizados.
- **`V2__insert_initial_data.sql`:** Carga inicial determinística com usuários de teste (`admin`, `veterinario`, `tutor`, `kauan`), clínicas parceiras e prontuários completos para **Rex** (Golden Retriever), **Mia** (Siamês) e **Thor** (Bulldog Francês).
- **`V3__sprint4_oauth2_telemetria_auditoria.sql`:** Evolução não-destrutiva na Sprint 4 adicionando suporte a OAuth2 em `TB_USUARIO`, criando a tabela de telemetria IoT/IA (`TB_TELEMETRIA_IOT`) e a tabela de auditoria clínica (`TB_AUDITORIA_PET`).

## Justificativa e Consequências
- **Reprodutibilidade Zero-Config:** Qualquer avaliador ou container em nuvem sobe a aplicação já populada e pronta para demonstração imediata.
- **Validação Estrita de Mapeamento Objeto-Relacional:** O modo `validate` do Hibernate garante que 100% das entidades JPA correspondem exatamente às colunas SQL versionadas pelo Flyway.
