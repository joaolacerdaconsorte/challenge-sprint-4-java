package br.com.fiap.pet360.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_AUDITORIA_PET")
public class AuditoriaPet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_AUDITORIA")
    private UUID id;

    @Column(name = "ID_PET", nullable = false)
    private UUID petId;

    @Column(name = "NM_PET", nullable = false, length = 100)
    private String nomePet;

    @Column(name = "OPERACAO", nullable = false, length = 20)
    private String operacao;

    @Column(name = "VALORES_ANTIGOS", length = 500)
    private String valoresAntigos;

    @Column(name = "VALORES_NOVOS", length = 500)
    private String valoresNovos;

    @Column(name = "USUARIO_RESPONSAVEL", nullable = false, length = 80)
    private String usuarioResponsavel;

    @Column(name = "DT_EVENTO", nullable = false)
    private LocalDateTime dataEvento = LocalDateTime.now();

    public AuditoriaPet() {
    }

    public AuditoriaPet(UUID petId, String nomePet, String operacao, String valoresAntigos, String valoresNovos, String usuarioResponsavel) {
        this.petId = petId;
        this.nomePet = nomePet;
        this.operacao = operacao;
        this.valoresAntigos = valoresAntigos;
        this.valoresNovos = valoresNovos;
        this.usuarioResponsavel = usuarioResponsavel;
        this.dataEvento = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public String getNomePet() {
        return nomePet;
    }

    public void setNomePet(String nomePet) {
        this.nomePet = nomePet;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getValoresAntigos() {
        return valoresAntigos;
    }

    public void setValoresAntigos(String valoresAntigos) {
        this.valoresAntigos = valoresAntigos;
    }

    public String getValoresNovos() {
        return valoresNovos;
    }

    public void setValoresNovos(String valoresNovos) {
        this.valoresNovos = valoresNovos;
    }

    public String getUsuarioResponsavel() {
        return usuarioResponsavel;
    }

    public void setUsuarioResponsavel(String usuarioResponsavel) {
        this.usuarioResponsavel = usuarioResponsavel;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(LocalDateTime dataEvento) {
        this.dataEvento = dataEvento;
    }
}
