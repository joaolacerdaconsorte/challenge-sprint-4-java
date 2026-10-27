package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.TelemetriaIotRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.TelemetriaIot;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TelemetriaIotRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CopilotoIaTelemetriaService {

    private final TelemetriaIotRepository telemetriaIotRepository;
    private final PetRepository petRepository;
    private final VacinaRepository vacinaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaRepository alertaRepository;

    public CopilotoIaTelemetriaService(TelemetriaIotRepository telemetriaIotRepository,
                                       PetRepository petRepository,
                                       VacinaRepository vacinaRepository,
                                       MedicamentoRepository medicamentoRepository,
                                       AlertaRepository alertaRepository) {
        this.telemetriaIotRepository = telemetriaIotRepository;
        this.petRepository = petRepository;
        this.vacinaRepository = vacinaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaRepository = alertaRepository;
    }

    @Transactional(readOnly = true)
    public List<TelemetriaIot> listarTodas() {
        return telemetriaIotRepository.findAllByOrderByDataLeituraDesc();
    }

    @Transactional(readOnly = true)
    public List<TelemetriaIot> listarPorPet(UUID petId) {
        return telemetriaIotRepository.findByPetIdOrderByDataLeituraDesc(petId);
    }

    @Transactional(readOnly = true)
    public Optional<TelemetriaIot> buscarUltimaPorPet(UUID petId) {
        return telemetriaIotRepository.findFirstByPetIdOrderByDataLeituraDesc(petId);
    }

    @CacheEvict(value = {"telemetrias", "alertas"}, allEntries = true)
    public TelemetriaIot processarTelemetria(TelemetriaIotRequest req) {
        Pet pet = petRepository.findById(req.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.getPetId()));

        List<Vacina> vacinasPendentes = vacinaRepository.findVencidasOuProximas(pet.getId(), LocalDate.now().plusDays(15));
        List<Medicamento> medicamentosAtivos = medicamentoRepository.findByPetIdAndAtivoTrueOrderByDataInicioDesc(pet.getId());

        int scoreIrp = calcularScoreIrp(
                pet,
                req.getTemperaturaCorporal(),
                req.getUmidadeAmbiente(),
                req.getAtividadeIob(),
                req.getBatimentosBpm(),
                vacinasPendentes.size(),
                medicamentosAtivos.size()
        );

        String nivelRisco = classificarNivelRisco(scoreIrp);
        String resumoSoap = gerarResumoSoap(pet, req, scoreIrp, nivelRisco, vacinasPendentes, medicamentosAtivos);
        String orientacaoTutor = gerarOrientacaoTutor(pet, req, scoreIrp, nivelRisco, vacinasPendentes);

        TelemetriaIot telemetria = new TelemetriaIot();
        telemetria.setPet(pet);
        telemetria.setTemperaturaCorporal(req.getTemperaturaCorporal());
        telemetria.setUmidadeAmbiente(req.getUmidadeAmbiente());
        telemetria.setAtividadeIob(req.getAtividadeIob());
        telemetria.setBatimentosBpm(req.getBatimentosBpm());
        telemetria.setScoreIrp(scoreIrp);
        telemetria.setNivelRisco(nivelRisco);
        telemetria.setOrigemDispositivo(req.getOrigemDispositivo());
        telemetria.setResumoSoap(resumoSoap);
        telemetria.setOrientacaoTutor(orientacaoTutor);
        telemetria.setDataLeitura(LocalDateTime.now());

        TelemetriaIot saved = telemetriaIotRepository.save(telemetria);

        if ("ALTO".equals(nivelRisco) || req.getTemperaturaCorporal().compareTo(new BigDecimal("39.40")) > 0) {
            String msgAlerta = String.format(
                    "[IoT + IA Preventiva] Risco %s (IRP %d/100) detectado para %s via %s: Temp %.2f °C, IOB %d%%, %d BPM. %s",
                    nivelRisco,
                    scoreIrp,
                    pet.getNome(),
                    req.getOrigemDispositivo(),
                    req.getTemperaturaCorporal(),
                    req.getAtividadeIob(),
                    req.getBatimentosBpm(),
                    orientacaoTutor
            );
            alertaRepository.save(new Alerta(TipoAlerta.BEM_ESTAR, msgAlerta, LocalDateTime.now(), pet, pet.getTutor()));
        }

        return saved;
    }

    public int calcularScoreIrp(Pet pet,
                                BigDecimal temp,
                                BigDecimal umidade,
                                int atividadeIob,
                                int bpm,
                                int qtdVacinasPendentes,
                                int qtdMedicamentosAtivos) {
        int score = 10;

        if (temp.compareTo(new BigDecimal("39.50")) >= 0 || temp.compareTo(new BigDecimal("37.20")) <= 0) {
            score += 35;
        } else if (temp.compareTo(new BigDecimal("39.10")) >= 0) {
            score += 18;
        }

        if (atividadeIob < 35) {
            score += 22;
        } else if (atividadeIob < 55) {
            score += 10;
        }

        if (bpm > 150 || bpm < 55) {
            score += 15;
        }

        if (umidade.compareTo(new BigDecimal("80.00")) > 0 || umidade.compareTo(new BigDecimal("30.00")) < 0) {
            score += 5;
        }

        if (pet.getDataNascimento() != null) {
            int idadeAnos = Period.between(pet.getDataNascimento(), LocalDate.now()).getYears();
            if (idadeAnos >= 7) {
                score += 12;
            }
        }

        score += Math.min(25, qtdVacinasPendentes * 15);
        score += Math.min(15, qtdMedicamentosAtivos * 8);

        if (pet.getObservacoes() != null && !pet.getObservacoes().isBlank()
                && !pet.getObservacoes().toLowerCase().contains("sem alergias")) {
            score += 8;
        }

        return Math.min(100, Math.max(5, score));
    }

    public String classificarNivelRisco(int scoreIrp) {
        if (scoreIrp >= 70) {
            return "ALTO";
        }
        if (scoreIrp >= 40) {
            return "MODERADO";
        }
        return "BAIXO";
    }

    private String gerarResumoSoap(Pet pet,
                                   TelemetriaIotRequest req,
                                   int scoreIrp,
                                   String nivelRisco,
                                   List<Vacina> vacinasPendentes,
                                   List<Medicamento> medicamentosAtivos) {
        String obsClinica = (pet.getObservacoes() != null && !pet.getObservacoes().isBlank())
                ? pet.getObservacoes()
                : "Sem comorbidades ou alergias relatadas";
        String statusVacinal = vacinasPendentes.isEmpty()
                ? "Calendario vacinal em dia"
                : vacinasPendentes.size() + " vacina(s) vencida(s)/proxima(s) (" + vacinasPendentes.get(0).getNome() + ")";
        String statusMedicacao = medicamentosAtivos.isEmpty()
                ? "Sem medicacao continua ativa"
                : "Em uso de " + medicamentosAtivos.get(0).getNome() + " (" + medicamentosAtivos.get(0).getDosagem() + ")";

        return String.format(
                "[S] Monitoramento continuo via %s (%s - %s). Historico: %s. " +
                "[O] Temp: %.2f C, Umidade: %.1f%%, IOB (Atividade): %d%%, FC: %d BPM, Peso: %s kg. " +
                "[A] Indice de Risco Preventivo (IRP Random Forest/Rules) = %d/100 (%s). %s | %s. " +
                "[P] %s",
                req.getOrigemDispositivo(),
                pet.getEspecie(),
                pet.getRaca() != null ? pet.getRaca() : "SRD",
                obsClinica,
                req.getTemperaturaCorporal(),
                req.getUmidadeAmbiente(),
                req.getAtividadeIob(),
                req.getBatimentosBpm(),
                pet.getPeso(),
                scoreIrp,
                nivelRisco,
                statusVacinal,
                statusMedicacao,
                "ALTO".equals(nivelRisco)
                        ? "Acionar triagem prioritaria imediata, monitorar curva termica e revisar imunizacao."
                        : ("MODERADO".equals(nivelRisco)
                                ? "Manter acompanhamento preventivo quinzenal e revisar prescricoes ativas."
                                : "Manter protocolo preventivo anual e enriquecimento ambiental.")
        );
    }

    private String gerarOrientacaoTutor(Pet pet,
                                        TelemetriaIotRequest req,
                                        int scoreIrp,
                                        String nivelRisco,
                                        List<Vacina> vacinasPendentes) {
        String nomeTutor = pet.getTutor() != null ? pet.getTutor().getNome() : "Tutor";
        if ("ALTO".equals(nivelRisco)) {
            return String.format(
                    "Atencao %s: a coleira inteligente de %s detectou sinal de alerta (Temp %.2f C, IOB %d%%, IRP %d/100). Recomendamos avaliacao veterinaria prioritaria.",
                    nomeTutor, pet.getNome(), req.getTemperaturaCorporal(), req.getAtividadeIob(), scoreIrp
            );
        }
        if (!vacinasPendentes.isEmpty()) {
            return String.format(
                    "Ola %s! Os sinais vitais de %s estao monitorados (Temp %.2f C), mas ha reforco vacinal pendente (%s). Agende a atualizacao!",
                    nomeTutor, pet.getNome(), req.getTemperaturaCorporal(), vacinasPendentes.get(0).getNome()
            );
        }
        return String.format(
                "Ola %s! A telemetria de %s esta dentro da faixa saudavel (Temp %.2f C, Atividade %d%%, IRP %d/100 - %s).",
                nomeTutor, pet.getNome(), req.getTemperaturaCorporal(), req.getAtividadeIob(), scoreIrp, nivelRisco
        );
    }
}
