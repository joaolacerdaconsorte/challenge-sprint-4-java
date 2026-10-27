package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.security.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class LoginWebController {

    private final CustomOAuth2UserService customOAuth2UserService;

    public LoginWebController(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Authentication authentication,
                        Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Usuário, senha ou credencial OAuth2 inválidos. Verifique suas credenciais.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Você foi desconectado com sucesso.");
        }
        return "login";
    }

    @GetMapping("/oauth2/sandbox/{provider}")
    public String oauth2SandboxLogin(@PathVariable String provider,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        String normalizedProvider = ("google".equalsIgnoreCase(provider)) ? "google" : "github";
        Map<String, Object> attributes = "google".equals(normalizedProvider)
                ? Map.of(
                        "sub", "google-oauth2-1092837465",
                        "name", "Tutor Google OAuth2",
                        "login", "tutor.google",
                        "email", "joao.lacerda@email.com"
                )
                : Map.of(
                        "id", 565565,
                        "name", "João Vitor Lacerda (GitHub)",
                        "login", "joaolacerdaconsorte",
                        "email", "joao.lacerda@email.com"
                );

        OAuth2User oauth2User = customOAuth2UserService.processOAuth2User(normalizedProvider, attributes);
        OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                oauth2User,
                oauth2User.getAuthorities(),
                normalizedProvider
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Autenticado via OAuth2 (" + normalizedProvider.toUpperCase() + ") com sucesso! Usuário federado sincronizado em TB_USUARIO."
        );
        return "redirect:/dashboard";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado(Model model) {
        model.addAttribute("titulo", "Acesso Negado (403)");
        model.addAttribute("mensagem", "Seu perfil de usuário não possui permissão para acessar esta área restrita.");
        return "error/403";
    }
}
