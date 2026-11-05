package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.TelemetriaIotRequest;
import br.com.fiap.pet360.model.TelemetriaIot;
import br.com.fiap.pet360.service.CopilotoIaTelemetriaService;
import br.com.fiap.pet360.service.IntegracaoMultidisciplinarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Integração Multidisciplinar & Health", description = "Endpoints de Health Check Cloud, Telemetria IoT e Exportação JSON NoSQL")
public class HealthAndIntegrationRestController {

    private final IntegracaoMultidisciplinarService integracaoService;
    private final CopilotoIaTelemetriaService copilotoIaTelemetriaService;

    public HealthAndIntegrationRestController(IntegracaoMultidisciplinarService integracaoService,
                                              CopilotoIaTelemetriaService copilotoIaTelemetriaService) {
        this.integracaoService = integracaoService;
        this.copilotoIaTelemetriaService = copilotoIaTelemetriaService;
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check de Produção (Compatível com Render, Azure App Service e Docker)")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "Healthy");
        status.put("application", "PetCare 360 - CLYVO VET (Sprint 4 Java Advanced)");
        status.put("turma", "2TDSPW - FIAP 2026");
        status.put("oauth2", "ENABLED (GitHub & Google)");
        status.put("i18n", "ENABLED (pt_BR, en)");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    @GetMapping(value = "/api/integracao/pets/{petId}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Exporta documento JSON clínico do Pet (Equivalente FN_PET_JSON para MongoDB/Cosmos DB)")
    public ResponseEntity<String> exportarPetJson(@PathVariable UUID petId) {
        return ResponseEntity.ok(integracaoService.gerarPetJson(petId));
    }

    @PostMapping("/api/telemetria")
    @Operation(summary = "Ingestão de pacote de telemetria IoT da coleira inteligente ESP32")
    public ResponseEntity<Map<String, Object>> registrarTelemetriaApi(@Valid @RequestBody TelemetriaIotRequest req) {
        TelemetriaIot saved = copilotoIaTelemetriaService.processarTelemetria(req);
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "pet", saved.getPet().getNome(),
                "scoreIrp", saved.getScoreIrp(),
                "nivelRisco", saved.getNivelRisco(),
                "resumoSoap", saved.getResumoSoap(),
                "orientacaoTutor", saved.getOrientacaoTutor()
        ));
    }
}
