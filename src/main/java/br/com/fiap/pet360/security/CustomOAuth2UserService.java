package br.com.fiap.pet360.security;

import br.com.fiap.pet360.model.Role;
import br.com.fiap.pet360.model.Usuario;
import br.com.fiap.pet360.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsuarioRepository usuarioRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return processOAuth2User(registrationId, oauth2User.getAttributes());
    }

    @Transactional
    public OAuth2User processOAuth2User(String registrationId, Map<String, Object> attributes) {
        String provider = registrationId != null ? registrationId.toUpperCase() : "OAUTH2";
        String email = extractEmail(attributes, provider);
        String rawUsername = extractUsername(attributes, email, provider);

        Usuario usuario = resolveOrCreateUsuario(rawUsername, email, provider);

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));

        Map<String, Object> enrichedAttributes = new HashMap<>(attributes);
        enrichedAttributes.put("username", usuario.getUsername());
        enrichedAttributes.put("email", usuario.getEmail());
        enrichedAttributes.put("provider", provider);
        enrichedAttributes.put("role", usuario.getRole().name());

        return new DefaultOAuth2User(authorities, enrichedAttributes, "username");
    }

    private Usuario resolveOrCreateUsuario(String username, String email, String provider) {
        if (email != null && !email.isBlank()) {
            Optional<Usuario> byEmail = usuarioRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                Usuario existing = byEmail.get();
                existing.setOauthProvider(provider);
                return usuarioRepository.save(existing);
            }
        }

        Optional<Usuario> byUsername = usuarioRepository.findByUsername(username);
        if (byUsername.isPresent()) {
            Usuario existing = byUsername.get();
            existing.setOauthProvider(provider);
            if (existing.getEmail() == null && email != null) {
                existing.setEmail(email);
            }
            return usuarioRepository.save(existing);
        }

        Usuario novoUsuario = new Usuario(
                username,
                "{noop}OAUTH2_EXTERNAL_" + UUID.randomUUID(),
                Role.USER,
                provider,
                email
        );
        return usuarioRepository.save(novoUsuario);
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        Object emailObj = attributes.get("email");
        if (emailObj != null && !emailObj.toString().isBlank()) {
            return emailObj.toString().trim().toLowerCase();
        }
        Object loginObj = attributes.get("login");
        if (loginObj != null) {
            return loginObj.toString().trim().toLowerCase() + "@" + provider.toLowerCase() + ".oauth";
        }
        return "user@" + provider.toLowerCase() + ".oauth";
    }

    private String extractUsername(Map<String, Object> attributes, String email, String provider) {
        Object login = attributes.get("login");
        if (login != null && !login.toString().isBlank()) {
            return login.toString().trim();
        }
        Object name = attributes.get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString().trim().replaceAll("\\s+", ".").toLowerCase();
        }
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return provider.toLowerCase() + "_user";
    }
}
