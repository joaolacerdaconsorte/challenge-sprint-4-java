package br.com.fiap.pet360.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_TELEMETRIA_IOT")
public class TelemetriaIot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_TELEMETRIA")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PET", nullable = false)
    private Pet pet;

    @Column(name = "TEMPERATURA_CORPORAL", nullable = false, precision = 4, scale = 2)
    private BigDecimal temperaturaCorporal;

    @Column(name = "UMIDADE_AMBIENTE", nullable = false, precision = 5, scale = 2)
    private BigDecimal umidadeAmbiente;

    @Column(name = "ATIVIDADE_IOB", nullable = false)
    private Integer atividadeIob;

    @Column(name = "BATIMENTOS_BPM", nullable = false)
    private Integer batimentosBpm;

    @Column(name = "NIVEL_RISCO", nullable = false, length = 20)
    private String nivelRisco;

    @Column(name = "SCORE_IRP", nullable = false)
    private Integer scoreIrp;

    @Column(name = "ORIGEM_DISPOSITIVO", nullable = false, length = 80)
    private String origemDispositivo;

    @Column(name = "RESUMO_SOAP", length = 1000)
    private String resumoSoap;

    @Column(name = "ORIENTACAO_TUTOR", length = 600)
    private String orientacaoTutor;

    @Column(name = "DT_LEITURA", nullable = false)
    private LocalDateTime dataLeitura = LocalDateTime.now();

    public TelemetriaIot() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public BigDecimal getTemperaturaCorporal() {
        return temperaturaCorporal;
    }

    public void setTemperaturaCorporal(BigDecimal temperaturaCorporal) {
        this.temperaturaCorporal = temperaturaCorporal;
    }

    public BigDecimal getUmidadeAmbiente() {
        return umidadeAmbiente;
    }

    public void setUmidadeAmbiente(BigDecimal umidadeAmbiente) {
        this.umidadeAmbiente = umidadeAmbiente;
    }

    public Integer getAtividadeIob() {
        return atividadeIob;
    }

    public void setAtividadeIob(Integer atividadeIob) {
        this.atividadeIob = atividadeIob;
    }

    public Integer getBatimentosBpm() {
        return batimentosBpm;
    }

    public void setBatimentosBpm(Integer batimentosBpm) {
        this.batimentosBpm = batimentosBpm;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public Integer getScoreIrp() {
        return scoreIrp;
    }

    public void setScoreIrp(Integer scoreIrp) {
        this.scoreIrp = scoreIrp;
    }

    public String getOrigemDispositivo() {
        return origemDispositivo;
    }

    public void setOrigemDispositivo(String origemDispositivo) {
        this.origemDispositivo = origemDispositivo;
    }

    public String getResumoSoap() {
        return resumoSoap;
    }

    public void setResumoSoap(String resumoSoap) {
        this.resumoSoap = resumoSoap;
    }

    public String getOrientacaoTutor() {
        return orientacaoTutor;
    }

    public void setOrientacaoTutor(String orientacaoTutor) {
        this.orientacaoTutor = orientacaoTutor;
    }

    public LocalDateTime getDataLeitura() {
        return dataLeitura;
    }

    public void setDataLeitura(LocalDateTime dataLeitura) {
        this.dataLeitura = dataLeitura;
    }
}
