package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.AtendimentoClinicoForm;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.AuditoriaPet;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.StatusConsulta;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.AuditoriaPetRepository;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class AtendimentoClinicoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VacinaRepository vacinaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaRepository alertaRepository;
    private final AuditoriaPetRepository auditoriaPetRepository;

    public AtendimentoClinicoService(AgendamentoRepository agendamentoRepository,
                                     ConsultaRepository consultaRepository,
                                     PetRepository petRepository,
                                     VacinaRepository vacinaRepository,
                                     MedicamentoRepository medicamentoRepository,
                                     AlertaRepository alertaRepository,
                                     AuditoriaPetRepository auditoriaPetRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.consultaRepository = consultaRepository;
        this.petRepository = petRepository;
        this.vacinaRepository = vacinaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaRepository = alertaRepository;
        this.auditoriaPetRepository = auditoriaPetRepository;
    }

    @CacheEvict(value = {"agendamentos", "consultas", "pets", "vacinas", "medicamentos", "alertas", "auditorias"}, allEntries = true)
    public Consulta realizarAtendimento(AtendimentoClinicoForm form) {
        Agendamento agendamento = agendamentoRepository.findById(form.getAgendamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", form.getAgendamentoId()));

        if (agendamento.getStatus() == StatusAgendamento.REALIZADO) {
            throw new BusinessException("Este agendamento já foi realizado anteriormente.");
        }

        Pet pet = agendamento.getPet();
        BigDecimal pesoAnterior = pet.getPeso();

        pet.setPeso(form.getPesoAtual());
        petRepository.save(pet);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String responsavel = (auth != null && auth.getName() != null) ? auth.getName() : "veterinario";
        auditoriaPetRepository.save(new AuditoriaPet(
                pet.getId(),
                pet.getNome(),
                "UPDATE",
                "PESO=" + pesoAnterior + "kg",
                "PESO=" + form.getPesoAtual() + "kg (Atualizado em Atendimento Clinico: " + form.getMotivo() + ")",
                responsavel
        ));

        Consulta consulta = new Consulta(
                LocalDateTime.now(),
                StatusConsulta.REALIZADA,
                form.getMotivo(),
                form.getValor(),
                pet,
                agendamento.getClinica()
        );
        consulta.setDiagnostico(form.getDiagnostico());
        consulta = consultaRepository.save(consulta);

        if (form.isAplicarVacina() && form.getNomeVacina() != null && !form.getNomeVacina().isBlank()) {
            int diasProxima = form.getDiasProximaDose() != null ? form.getDiasProximaDose() : 365;
            LocalDate dataProxima = LocalDate.now().plusDays(diasProxima);

            Vacina vacina = new Vacina(form.getNomeVacina(), LocalDate.now(), dataProxima, pet);
            vacina.setLote(form.getLoteVacina());
            vacina.setConsulta(consulta);
            vacinaRepository.save(vacina);

            String descAlertaVacina = "Vacina " + vacina.getNome() + " aplicada em " + pet.getNome() +
                    ". Próximo reforço previsto para " + dataProxima + ".";
            alertaRepository.save(new Alerta(TipoAlerta.VACINA, descAlertaVacina, LocalDateTime.now(), pet, pet.getTutor()));
        }

        if (form.isPrescreverMedicamento() && form.getNomeMedicamento() != null && !form.getNomeMedicamento().isBlank()) {
            int duracao = form.getDuracaoDias() != null ? form.getDuracaoDias() : 7;
            LocalDate dataFim = LocalDate.now().plusDays(duracao);

            Medicamento med = new Medicamento(
                    form.getNomeMedicamento(),
                    form.getDosagem(),
                    form.getFrequencia(),
                    LocalDate.now(),
                    dataFim,
                    form.getCustoMedicamento(),
                    pet
            );
            med.setConsulta(consulta);
            med.setAtivo(true);
            medicamentoRepository.save(med);

            String descAlertaMed = "Prescrição médica para " + pet.getNome() + ": " + med.getNome() +
                    " (" + med.getDosagem() + ", " + med.getFrequencia() + ") por " + duracao + " dias.";
            alertaRepository.save(new Alerta(TipoAlerta.MEDICAMENTO, descAlertaMed, LocalDateTime.now(), pet, pet.getTutor()));
        }

        agendamento.setStatus(StatusAgendamento.REALIZADO);
        agendamentoRepository.save(agendamento);

        return consulta;
    }
}
