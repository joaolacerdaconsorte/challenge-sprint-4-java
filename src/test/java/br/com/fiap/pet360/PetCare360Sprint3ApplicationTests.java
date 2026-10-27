package br.com.fiap.pet360;

import br.com.fiap.pet360.dto.AtendimentoClinicoForm;
import br.com.fiap.pet360.dto.TelemetriaIotRequest;
import br.com.fiap.pet360.dto.TriagemAgendamentoRequest;
import br.com.fiap.pet360.dto.TriagemAgendamentoResult;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TelemetriaIot;
import br.com.fiap.pet360.model.TipoAgendamento;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AuditoriaPetRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TelemetriaIotRepository;
import br.com.fiap.pet360.repository.TutorRepository;
import br.com.fiap.pet360.repository.UsuarioRepository;
import br.com.fiap.pet360.security.CustomOAuth2UserService;
import br.com.fiap.pet360.service.AtendimentoClinicoService;
import br.com.fiap.pet360.service.CopilotoIaTelemetriaService;
import br.com.fiap.pet360.service.IntegracaoMultidisciplinarService;
import br.com.fiap.pet360.service.TriagemAgendamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetCare360Sprint3ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private TelemetriaIotRepository telemetriaIotRepository;

    @Autowired
    private AuditoriaPetRepository auditoriaPetRepository;

    @Autowired
    private TriagemAgendamentoService triagemAgendamentoService;

    @Autowired
    private AtendimentoClinicoService atendimentoClinicoService;

    @Autowired
    private CopilotoIaTelemetriaService copilotoIaTelemetriaService;

    @Autowired
    private IntegracaoMultidisciplinarService integracaoMultidisciplinarService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("1. Contexto Spring Boot e Migrations Flyway (V1, V2 e V3) devem carregar dados iniciais")
    void deveCarregarContextoEMigrationsFlyway() {
        assertThat(usuarioRepository.count()).isGreaterThanOrEqualTo(5);
        assertThat(tutorRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(petRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(clinicaRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(telemetriaIotRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(auditoriaPetRepository.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("2. Rota protegida (/dashboard) sem autenticação deve redirecionar para /login")
    void deveRedirecionarParaLoginQuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("3. Página de Login deve exibir botões OAuth2 (GitHub e Google) e suportar Internacionalização (PT-BR / EN)")
    void deveRenderizarBotoesOAuth2EInternacionalizacaoNoLogin() throws Exception {
        mockMvc.perform(get("/login").param("lang", "pt_BR"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/oauth2/authorization/github")))
                .andExpect(content().string(containsString("/oauth2/authorization/google")))
                .andExpect(content().string(containsString("Continuar com GitHub")));

        mockMvc.perform(get("/login").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Continue with GitHub")))
                .andExpect(content().string(containsString("Continue with Google")));
    }

    @Test
    @DisplayName("4. Serviço OAuth2 (CustomOAuth2UserService) deve provisionar e sincronizar usuário federado")
    void deveProvisionarUsuarioOAuth2() {
        OAuth2User oauth2User = customOAuth2UserService.processOAuth2User("github", Map.of(
                "login", "murillo.carapia.oauth",
                "name", "Murillo Fernandes Carapia",
                "email", "murillo.carapia@fiap.com.br"
        ));

        assertThat(oauth2User).isNotNull();
        assertThat(oauth2User.getName()).isEqualTo("murillo.carapia.oauth");
        assertThat(usuarioRepository.findByUsername("murillo.carapia.oauth"))
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getOauthProvider()).isEqualTo("GITHUB"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("5. Usuário autenticado com perfil USER deve acessar o Dashboard com sucesso")
    void devePermitirAcessoAoDashboardQuandoAutenticado() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("6. Controle de Acesso (RBAC): Perfil USER não deve acessar rota exclusiva de ADMIN (/clinicas/nova)")
    void deveBloquearRotaDeAdminParaPerfilUser() throws Exception {
        mockMvc.perform(get("/clinicas/nova"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Fluxo Não-CRUD 1: Triagem e Agendamento Inteligente deve registrar agendamento e gerar alertas preventivos")
    void deveExecutarFluxoTriagemEAgendamentoInteligente() {
        UUID petRexId = UUID.fromString("fe111111-1111-1111-1111-111111111111");
        UUID clinicaId = UUID.fromString("ca111111-1111-1111-1111-111111111111");

        TriagemAgendamentoRequest req = new TriagemAgendamentoRequest(
                petRexId,
                clinicaId,
                LocalDateTime.now().plusDays(5),
                TipoAgendamento.CONSULTA,
                "Check-up preventivo do Rex"
        );

        TriagemAgendamentoResult resultado = triagemAgendamentoService.processarAgendamentoComTriagem(req);

        assertThat(resultado).isNotNull();
        assertThat(resultado.agendamento().getId()).isNotNull();
        assertThat(resultado.alertaCriado()).isTrue();
        assertThat(resultado.alertaDescricao()).contains("Rex");
    }

    @Test
    @DisplayName("8. Fluxo Não-CRUD 2: Atendimento Clínico Integrado deve concluir agendamento, gerar consulta, vacina, medicamento e auditoria")
    void deveExecutarFluxoAtendimentoClinicoIntegradoEAuditoria() {
        UUID agendamentoId = UUID.fromString("e1111111-1111-1111-1111-111111111111");
        long auditoriasAntes = auditoriaPetRepository.count();

        AtendimentoClinicoForm form = new AtendimentoClinicoForm();
        form.setAgendamentoId(agendamentoId);
        form.setPesoAtual(new BigDecimal("29.10"));
        form.setMotivo("Atendimento de rotina e reforço vacinal");
        form.setDiagnostico("Animal saudável e apto para imunização");
        form.setValor(new BigDecimal("220.00"));
        form.setAplicarVacina(true);
        form.setNomeVacina("Antirrábica Anual");
        form.setLoteVacina("LOTE-2026-FIAP");
        form.setDiasProximaDose(365);
        form.setPrescreverMedicamento(true);
        form.setNomeMedicamento("Suplemento Ômega 3 Pet");
        form.setDosagem("1 cápsula");
        form.setFrequencia("1x ao dia");
        form.setDuracaoDias(30);
        form.setCustoMedicamento(new BigDecimal("85.00"));

        Consulta consulta = atendimentoClinicoService.realizarAtendimento(form);

        assertThat(consulta.getId()).isNotNull();

        Pet petAtualizado = petRepository.findById(consulta.getPet().getId()).orElseThrow();
        assertThat(petAtualizado.getPeso()).isEqualByComparingTo("29.10");

        var agendamentoAtualizado = agendamentoRepository.findById(agendamentoId).orElseThrow();
        assertThat(agendamentoAtualizado.getStatus()).isEqualTo(StatusAgendamento.REALIZADO);
        assertThat(auditoriaPetRepository.count()).isEqualTo(auditoriasAntes + 1);
    }

    @Test
    @DisplayName("9. Fluxo Não-CRUD 3: Copiloto Clínico IA & Telemetria IoT deve calcular Score IRP, gerar prontuário SOAP e alerta crítico")
    void deveProcessarTelemetriaIotEGerarLaudoSoap() {
        UUID petRexId = UUID.fromString("fe111111-1111-1111-1111-111111111111");

        TelemetriaIotRequest req = new TelemetriaIotRequest();
        req.setPetId(petRexId);
        req.setTemperaturaCorporal(new BigDecimal("39.90"));
        req.setUmidadeAmbiente(new BigDecimal("72.00"));
        req.setAtividadeIob(22);
        req.setBatimentosBpm(155);
        req.setOrigemDispositivo("ESP32-WOKWI-TEST-MQTT");

        TelemetriaIot telemetria = copilotoIaTelemetriaService.processarTelemetria(req);

        assertThat(telemetria.getId()).isNotNull();
        assertThat(telemetria.getScoreIrp()).isGreaterThanOrEqualTo(70);
        assertThat(telemetria.getNivelRisco()).isEqualTo("ALTO");
        assertThat(telemetria.getResumoSoap()).contains("[S]", "[O]", "[A]", "[P]");
        assertThat(telemetria.getOrientacaoTutor()).contains("Rex");
    }

    @Test
    @DisplayName("10. Fluxo Não-CRUD 4: Integração Multidisciplinar deve exportar JSON NoSQL (FN_PET_JSON) e calcular risco vacinal")
    void deveGerarJsonPetECalcularRiscoVacinal() {
        UUID petRexId = UUID.fromString("fe111111-1111-1111-1111-111111111111");

        String risco = integracaoMultidisciplinarService.calcularRiscoVacinal(petRexId);
        String json = integracaoMultidisciplinarService.gerarPetJson(petRexId);

        assertThat(risco).isEqualTo("ALTO_RISCO_VACINA_VENCIDA");
        assertThat(json).contains("clyvo-vet-2026.sprint4", "Rex", "Labrador Retriever", "ALTO_RISCO_VACINA_VENCIDA");
    }

    @Test
    @DisplayName("11. Health Check de Produção (/health e /actuator/health) deve responder 200 OK")
    void deveResponderHealthCheckPublico() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Healthy")));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    @DisplayName("12. Callback OAuth2 Sandbox (/oauth2/sandbox/github) deve autenticar usuário federado e redirecionar para /dashboard")
    void deveAutenticarViaCallbackOAuth2Sandbox() throws Exception {
        mockMvc.perform(get("/oauth2/sandbox/github"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser(username = "veterinario", roles = {"VETERINARIO"})
    @DisplayName("13. Tela Web do Copiloto IA & Telemetria IoT (/ia-telemetria) deve renderizar prontuários SOAP e leituras ESP32")
    void deveRenderizarPaginaCopilotoIaTelemetria() throws Exception {
        mockMvc.perform(get("/ia-telemetria"))
                .andExpect(status().isOk())
                .andExpect(view().name("ia/telemetria"))
                .andExpect(content().string(containsString("ESP32-WOKWI-COLAR-REX")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("14. Tela Web do Hub Multidisciplinar (/integracao) deve renderizar JSON NoSQL e trilha de auditoria TB_AUDITORIA_PET")
    void deveRenderizarPaginaHubMultidisciplinar() throws Exception {
        mockMvc.perform(get("/integracao"))
                .andExpect(status().isOk())
                .andExpect(view().name("integracao/hub"))
                .andExpect(content().string(containsString("clyvo-vet-2026.sprint4")))
                .andExpect(content().string(containsString("TB_AUDITORIA_PET")));
    }
}
