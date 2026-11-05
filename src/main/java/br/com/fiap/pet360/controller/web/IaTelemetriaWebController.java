package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.dto.TelemetriaIotRequest;
import br.com.fiap.pet360.model.TelemetriaIot;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.service.CopilotoIaTelemetriaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ia-telemetria")
public class IaTelemetriaWebController {

    private final CopilotoIaTelemetriaService copilotoIaTelemetriaService;
    private final PetRepository petRepository;

    public IaTelemetriaWebController(CopilotoIaTelemetriaService copilotoIaTelemetriaService,
                                     PetRepository petRepository) {
        this.copilotoIaTelemetriaService = copilotoIaTelemetriaService;
        this.petRepository = petRepository;
    }

    @GetMapping
    public String painel(Model model) {
        if (!model.containsAttribute("telemetriaForm")) {
            model.addAttribute("telemetriaForm", new TelemetriaIotRequest());
        }
        model.addAttribute("pets", petRepository.findAll(Sort.by("nome").ascending()));
        model.addAttribute("telemetrias", copilotoIaTelemetriaService.listarTodas());
        return "ia/telemetria";
    }

    @PostMapping("/registrar")
    public String registrar(@Valid @ModelAttribute("telemetriaForm") TelemetriaIotRequest form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pets", petRepository.findAll(Sort.by("nome").ascending()));
            model.addAttribute("telemetrias", copilotoIaTelemetriaService.listarTodas());
            return "ia/telemetria";
        }

        TelemetriaIot saved = copilotoIaTelemetriaService.processarTelemetria(form);
        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Pacote MQTT/IoT processado pelo Copiloto Clínico IA! Pet: " + saved.getPet().getNome()
                        + " | IRP = " + saved.getScoreIrp() + "/100 (" + saved.getNivelRisco() + ")."
        );
        return "redirect:/ia-telemetria";
    }
}
