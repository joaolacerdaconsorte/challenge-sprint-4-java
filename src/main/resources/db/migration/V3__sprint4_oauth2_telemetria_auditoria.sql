-- =====================================================================
-- PetCare 360 (CLYVO VET) - Flyway V3: OAuth2, Telemetria IoT/IA & Auditoria
-- =====================================================================

-- 1. Evolucao da tabela TB_USUARIO para suporte a OAuth2 (Google / GitHub)
ALTER TABLE TB_USUARIO ADD COLUMN OAUTH_PROVIDER VARCHAR(30) DEFAULT 'LOCAL';
ALTER TABLE TB_USUARIO ADD COLUMN EMAIL VARCHAR(100);

UPDATE TB_USUARIO SET EMAIL = 'admin@petcare360.com.br', OAUTH_PROVIDER = 'LOCAL' WHERE USERNAME = 'admin';
UPDATE TB_USUARIO SET EMAIL = 'vet@petcare360.com.br', OAUTH_PROVIDER = 'LOCAL' WHERE USERNAME = 'vet';
UPDATE TB_USUARIO SET EMAIL = 'user@petcare360.com.br', OAUTH_PROVIDER = 'LOCAL' WHERE USERNAME = 'user';

-- Usuarios adicionais para conveniencia de demonstracao
INSERT INTO TB_USUARIO (ID_USUARIO, USERNAME, PASSWORD_HASH, ROLE, OAUTH_PROVIDER, EMAIL)
VALUES ('44444444-4444-4444-4444-444444444444', 'veterinario', '$2a$10$zOm7DavqpxtahjmX3oCxV.ABLkRCQYz3JdbAQTSr1mjgTQjoyUPQa', 'VETERINARIO', 'LOCAL', 'veterinario@petcare360.com.br');

INSERT INTO TB_USUARIO (ID_USUARIO, USERNAME, PASSWORD_HASH, ROLE, OAUTH_PROVIDER, EMAIL)
VALUES ('55555555-5555-5555-5555-555555555555', 'tutor', '$2a$10$5L.jLejX/TD666TLPdxbYeil7LZFgf.pd/f/6VcakevE2bjVXgGUi', 'USER', 'LOCAL', 'joao.lacerda@email.com');

-- 2. Atualizacao das datas de agendamentos pendentes para manter a fila clinica ativa na Sprint 4
UPDATE TB_AGENDAMENTO SET DT_AGENDAMENTO = '2026-11-15 14:00:00' WHERE ID_AGENDAMENTO = 'e1111111-1111-1111-1111-111111111111';
UPDATE TB_AGENDAMENTO SET DT_AGENDAMENTO = '2026-11-18 10:30:00' WHERE ID_AGENDAMENTO = 'e2222222-2222-2222-2222-222222222222';

-- 3. Tabela de Telemetria IoT e Predicao IA (Integracao Disruptive Architectures: ESP32 Wokwi + MQTT + Random Forest IRP + Copiloto SOAP)
CREATE TABLE TB_TELEMETRIA_IOT (
    ID_TELEMETRIA UUID PRIMARY KEY,
    ID_PET UUID NOT NULL,
    TEMPERATURA_CORPORAL NUMERIC(4, 2) NOT NULL,
    UMIDADE_AMBIENTE NUMERIC(5, 2) NOT NULL,
    ATIVIDADE_IOB INTEGER NOT NULL,
    BATIMENTOS_BPM INTEGER NOT NULL,
    NIVEL_RISCO VARCHAR(20) NOT NULL,
    SCORE_IRP INTEGER NOT NULL,
    ORIGEM_DISPOSITIVO VARCHAR(80) NOT NULL,
    RESUMO_SOAP VARCHAR(1000),
    ORIENTACAO_TUTOR VARCHAR(600),
    DT_LEITURA TIMESTAMP NOT NULL,
    CONSTRAINT FK_TELEMETRIA_PET FOREIGN KEY (ID_PET) REFERENCES TB_PET (ID_PET) ON DELETE CASCADE
);

CREATE INDEX IDX_TELEMETRIA_PET ON TB_TELEMETRIA_IOT (ID_PET);
CREATE INDEX IDX_TELEMETRIA_RISCO ON TB_TELEMETRIA_IOT (NIVEL_RISCO);

-- 4. Tabela de Auditoria Automatica de Pets (Espelhamento da Trigger Oracle TRG_AUDITORIA_PET - Database Sprint 3)
CREATE TABLE TB_AUDITORIA_PET (
    ID_AUDITORIA UUID PRIMARY KEY,
    ID_PET UUID NOT NULL,
    NM_PET VARCHAR(100) NOT NULL,
    OPERACAO VARCHAR(20) NOT NULL,
    VALORES_ANTIGOS VARCHAR(500),
    VALORES_NOVOS VARCHAR(500),
    USUARIO_RESPONSAVEL VARCHAR(80) NOT NULL,
    DT_EVENTO TIMESTAMP NOT NULL
);

CREATE INDEX IDX_AUDITORIA_PET_DT ON TB_AUDITORIA_PET (DT_EVENTO DESC);

-- 5. Carga inicial de Telemetria IoT (Simulador Wokwi ESP32 + DHT22 + Acelerometro IOB)
INSERT INTO TB_TELEMETRIA_IOT (
    ID_TELEMETRIA, ID_PET, TEMPERATURA_CORPORAL, UMIDADE_AMBIENTE, ATIVIDADE_IOB, BATIMENTOS_BPM,
    NIVEL_RISCO, SCORE_IRP, ORIGEM_DISPOSITIVO, RESUMO_SOAP, ORIENTACAO_TUTOR, DT_LEITURA
) VALUES
(
    'a0000000-0000-0000-0000-000000000001',
    'fe111111-1111-1111-1111-111111111111',
    39.70, 68.50, 28, 128,
    'ALTO', 78, 'ESP32-WOKWI-COLAR-REX (MQTT)',
    '[S] Tutor relata coceira e menor disposicao fisica; alergico a picada de pulgas. [O] Temp: 39.70 C (hipertermia leve), BPM: 128, IOB: 28% (letargia), Peso: 28.50 kg. [A] Indice de Risco Preventivo (IRP) = 78/100 (ALTO). Vacina Antirrabica Canina vencida em 08/2026 + alteracao termica. [P] Priorizar avaliacao clinica e atualizar imunizacao antirrabica apos estabilizacao.',
    'Atencao Ana Lima: a coleira inteligente do Rex detectou temperatura elevada (39.7 C) e vacina Antirrabica vencida. Mantenha-o hidratado e confirme o atendimento prioritario.',
    '2026-10-25 14:20:00'
),
(
    'a0000000-0000-0000-0000-000000000002',
    'fe333333-3333-3333-3333-333333333333',
    38.60, 55.00, 74, 88,
    'MODERADO', 48, 'ESP32-WOKWI-COLAR-THOR (MQTT)',
    '[S] Animal em uso continuo de Bravecto e historico de displasia leve de quadril. [O] Temp: 38.60 C (normotermia), BPM: 88, IOB: 74% (atividade preservada), Peso: 32.00 kg. [A] Indice de Risco Preventivo (IRP) = 48/100 (MODERADO) devido a idade senior (2019) e medicacao ativa. [P] Manter monitoramento ortopedico e controle antiparasitario.',
    'Ola Bruno Souza! Os sinais vitais do Thor estao estaveis (38.6 C e boa atividade). Mantenha o acompanhamento preventivo.',
    '2026-10-26 09:15:00'
),
(
    'a0000000-0000-0000-0000-000000000003',
    'fe222222-2222-2222-2222-222222222222',
    38.40, 52.00, 82, 140,
    'BAIXO', 18, 'ESP32-WOKWI-COLAR-MIA (MQTT)',
    '[S] Felino assintomatico em acompanhamento preventivo. [O] Temp: 38.40 C (normal para felinos), BPM: 140, IOB: 82% (alta mobilidade), Peso: 4.20 kg. [A] Indice de Risco Preventivo (IRP) = 18/100 (BAIXO). Calendario vacinal V4 em dia ate 06/2027. [P] Manter racao especial urinaria e escovacao diaria.',
    'Tudo otimo com a Mia! Temperatura (38.4 C) e atividade fisica dentro do padrao ideal para felinos.',
    '2026-10-26 16:40:00'
);

-- 6. Carga inicial de Auditoria (Espelhamento da TRG_AUDITORIA_PET)
INSERT INTO TB_AUDITORIA_PET (
    ID_AUDITORIA, ID_PET, NM_PET, OPERACAO, VALORES_ANTIGOS, VALORES_NOVOS, USUARIO_RESPONSAVEL, DT_EVENTO
) VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'fe111111-1111-1111-1111-111111111111',
    'Rex',
    'INSERT',
    NULL,
    'ESPECIE=CACHORRO | RACA=Labrador Retriever | PESO=28.00kg | OBS=Alergico a picada de pulgas',
    'FLYWAY_SEED',
    '2026-01-10 09:00:00'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'fe111111-1111-1111-1111-111111111111',
    'Rex',
    'UPDATE',
    'PESO=28.00kg | OBS=Alergico a picada de pulgas',
    'PESO=28.50kg | OBS=Alergico a picada de pulgas (Atualizado em Check-up Anual)',
    'vet',
    '2026-01-10 14:30:00'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'fe333333-3333-3333-3333-333333333333',
    'Thor',
    'INSERT',
    NULL,
    'ESPECIE=CACHORRO | RACA=Golden Retriever | PESO=32.00kg | OBS=Historico de displasia leve de quadril',
    'FLYWAY_SEED',
    '2026-07-01 15:00:00'
);
