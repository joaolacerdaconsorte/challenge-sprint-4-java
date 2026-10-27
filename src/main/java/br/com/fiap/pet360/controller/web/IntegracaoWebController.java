package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.service.IntegracaoMultidisciplinarService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/integracao")
public class IntegracaoWebController {

    private final IntegracaoMultidisciplinarService integracaoService;
    private final PetRepository petRepository;

    public IntegracaoWebController(IntegracaoMultidisciplinarService integracaoService,
                                   PetRepository petRepository) {
        this.integracaoService = integracaoService;
        this.petRepository = petRepository;
    }

    @GetMapping
    public String hub(@RequestParam(value = "petId", required = false) UUID petId, Model model) {
        List<Pet> pets = petRepository.findAll(Sort.by("nome").ascending());
        model.addAttribute("pets", pets);

        if (!pets.isEmpty()) {
            UUID selecionadoId = (petId != null) ? petId : pets.get(0).getId();
            model.addAttribute("petSelecionadoId", selecionadoId);
            model.addAttribute("petJson", integracaoService.gerarPetJson(selecionadoId));
            model.addAttribute("riscoVacinal", integracaoService.calcularRiscoVacinal(selecionadoId));
        }

        model.addAttribute("auditorias", integracaoService.listarAuditorias());
        return "integracao/hub";
    }
}
