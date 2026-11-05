package br.com.fiap.pet360.service;

import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.AuditoriaPet;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.TelemetriaIot;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.AuditoriaPetRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TelemetriaIotRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class IntegracaoMultidisciplinarService {

    private final PetRepository petRepository;
    private final VacinaRepository vacinaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final TelemetriaIotRepository telemetriaIotRepository;
    private final AuditoriaPetRepository auditoriaPetRepository;
    private final ObjectMapper objectMapper;

    public IntegracaoMultidisciplinarService(PetRepository petRepository,
                                             VacinaRepository vacinaRepository,
                                             MedicamentoRepository medicamentoRepository,
                                             TelemetriaIotRepository telemetriaIotRepository,
                                             AuditoriaPetRepository auditoriaPetRepository,
                                             ObjectMapper objectMapper) {
        this.petRepository = petRepository;
        this.vacinaRepository = vacinaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.telemetriaIotRepository = telemetriaIotRepository;
        this.auditoriaPetRepository = auditoriaPetRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Equivalente Java da funcao PL/SQL FN_CALC_RISCO_VACINAL (Database Sprint 3 - Nota 95/100).
     */
    public String calcularRiscoVacinal(UUID petId) {
        List<Vacina> todasVacinas = vacinaRepository.findByPetIdOrderByDataAplicacaoDesc(petId);
        if (todasVacinas.isEmpty()) {
            return "SEM_REGISTRO_VACINAL";
        }
        LocalDate hoje = LocalDate.now();
        boolean temVencida = todasVacinas.stream()
                .anyMatch(v -> v.getDataProxima() != null && v.getDataProxima().isBefore(hoje));
        if (temVencida) {
            return "ALTO_RISCO_VACINA_VENCIDA";
        }
        boolean venceEm30Dias = todasVacinas.stream()
                .anyMatch(v -> v.getDataProxima() != null && !v.getDataProxima().isAfter(hoje.plusDays(30)));
        if (venceEm30Dias) {
            return "MODERADO_REFORCO_PROXIMO";
        }
        return "BAIXO_IMUNIZADO_EM_DIA";
    }

    /**
     * Equivalente Java da funcao PL/SQL FN_PET_JSON (PKG_CLYVO_VET - Database Sprint 3),
     * exportando documento JSON compativel com MongoDB/Azure Cosmos DB e Mobile React Native.
     */
    public String gerarPetJson(UUID petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        List<Vacina> vacinas = vacinaRepository.findByPetIdOrderByDataAplicacaoDesc(petId);
        List<Medicamento> medicamentos = medicamentoRepository.findByPetIdAndAtivoTrueOrderByDataInicioDesc(petId);
        Optional<TelemetriaIot> ultimaTelemetria = telemetriaIotRepository.findFirstByPetIdOrderByDataLeituraDesc(petId);

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("schema_version", "clyvo-vet-2026.sprint4");
        doc.put("id_pet", pet.getId().toString());
        doc.put("nm_pet", pet.getNome());
        doc.put("ds_especie", pet.getEspecie().name());
        doc.put("ds_raca", pet.getRaca());
        doc.put("nr_peso_kg", pet.getPeso());
        doc.put("dt_nascimento", pet.getDataNascimento() != null ? pet.getDataNascimento().toString() : null);
        doc.put("idade_anos", pet.getDataNascimento() != null
                ? Period.between(pet.getDataNascimento(), LocalDate.now()).getYears()
                : 0);
        doc.put("ds_observacoes_alergias", pet.getObservacoes());
        doc.put("status_risco_vacinal", calcularRiscoVacinal(petId));

        if (pet.getTutor() != null) {
            Map<String, Object> tutorMap = new LinkedHashMap<>();
            tutorMap.put("id_tutor", pet.getTutor().getId().toString());
            tutorMap.put("nm_tutor", pet.getTutor().getNome());
            tutorMap.put("ds_email", pet.getTutor().getEmail());
            tutorMap.put("nr_telefone", pet.getTutor().getTelefone());
            doc.put("tutor", tutorMap);
        }

        doc.put("vacinas_registradas", vacinas.stream().map(v -> Map.of(
                "nome", v.getNome(),
                "lote", v.getLote() != null ? v.getLote() : "N/A",
                "dt_aplicacao", v.getDataAplicacao().toString(),
                "dt_proxima_dose", v.getDataProxima() != null ? v.getDataProxima().toString() : "N/A"
        )).toList());

        doc.put("medicamentos_ativos", medicamentos.stream().map(m -> Map.of(
                "nome", m.getNome(),
                "dosagem", m.getDosagem() != null ? m.getDosagem() : "N/A",
                "frequencia", m.getFrequencia() != null ? m.getFrequencia() : "N/A"
        )).toList());

        ultimaTelemetria.ifPresent(t -> doc.put("telemetria_iot_recente", Map.of(
                "dispositivo", t.getOrigemDispositivo(),
                "temperatura_c", t.getTemperaturaCorporal(),
                "atividade_iob_pct", t.getAtividadeIob(),
                "batimentos_bpm", t.getBatimentosBpm(),
                "score_irp_ia", t.getScoreIrp(),
                "nivel_risco", t.getNivelRisco()
        )));

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            return "{\"erro\":\"Falha ao serializar JSON do pet\"}";
        }
    }

    public List<AuditoriaPet> listarAuditorias() {
        return auditoriaPetRepository.findAllByOrderByDataEventoDesc();
    }
}
