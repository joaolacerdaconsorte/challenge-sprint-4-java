package br.com.fiap.pet360.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class TelemetriaIotRequest {

    @NotNull(message = "Selecione o pet monitorado pela coleira IoT")
    private UUID petId;

    @NotNull(message = "Informe a temperatura corporal (°C)")
    @DecimalMin(value = "34.0", message = "Temperatura mínima fisiológica é 34.0 °C")
    @DecimalMax(value = "43.0", message = "Temperatura máxima fisiológica é 43.0 °C")
    private BigDecimal temperaturaCorporal = new BigDecimal("38.50");

    @NotNull(message = "Informe a umidade ambiente (%)")
    @DecimalMin(value = "10.0", message = "Umidade mínima é 10%")
    @DecimalMax(value = "100.0", message = "Umidade máxima é 100%")
    private BigDecimal umidadeAmbiente = new BigDecimal("60.00");

    @NotNull(message = "Informe o índice de atividade IOB (0 a 100%)")
    @Min(value = 0, message = "IOB mínimo é 0%")
    @Max(value = 100, message = "IOB máximo é 100%")
    private Integer atividadeIob = 65;

    @NotNull(message = "Informe os batimentos cardíacos (BPM)")
    @Min(value = 40, message = "BPM mínimo é 40")
    @Max(value = 240, message = "BPM máximo é 240")
    private Integer batimentosBpm = 95;

    @NotBlank(message = "Informe o identificador do dispositivo IoT")
    private String origemDispositivo = "ESP32-WOKWI-COLEIRA-MQTT";

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
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

    public String getOrigemDispositivo() {
        return origemDispositivo;
    }

    public void setOrigemDispositivo(String origemDispositivo) {
        this.origemDispositivo = origemDispositivo;
    }
}
