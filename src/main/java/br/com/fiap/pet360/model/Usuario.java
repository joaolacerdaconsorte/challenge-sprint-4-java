package br.com.fiap.pet360.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TB_USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_USUARIO")
    private UUID id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private Role role;

    @Column(name = "OAUTH_PROVIDER", length = 30)
    private String oauthProvider = "LOCAL";

    @Column(name = "EMAIL", length = 100)
    private String email;

    public Usuario() {
    }

    public Usuario(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.oauthProvider = "LOCAL";
    }

    public Usuario(String username, String passwordHash, Role role, String oauthProvider, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.oauthProvider = oauthProvider;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario u)) return false;
        return id != null && Objects.equals(id, u.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
