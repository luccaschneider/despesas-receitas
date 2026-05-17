package com.univates.despesasreceitas.entity;

import jakarta.persistence.*;

/**
 * Configuração de e-mail persistida (uma única linha, {@link #ID_FIXO}).
 */
@Entity
@Table(name = "configuracao_email")
public class ConfiguracaoEmail {

    public static final long ID_FIXO = 1L;

    @Id
    @Column(nullable = false)
    private Long id = ID_FIXO;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false, length = 150)
    private String destinatario = "admin@email.com";

    @Column(nullable = false, length = 150)
    private String remetente = "";

    @Column(name = "smtp_host", nullable = false, length = 255)
    private String smtpHost = "";

    @Column(name = "smtp_port", nullable = false)
    private int smtpPort = 587;

    @Column(name = "smtp_username", nullable = false, length = 255)
    private String smtpUsername = "";

    @Column(name = "smtp_password", nullable = false, length = 512)
    private String smtpPassword = "";

    @Column(name = "smtp_auth", nullable = false)
    private boolean smtpAuth = true;

    @Column(name = "smtp_starttls", nullable = false)
    private boolean smtpStarttls = true;

    @Column(name = "smtp_debug", nullable = false)
    private boolean smtpDebug = false;

    public ConfiguracaoEmail() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id != null ? id : ID_FIXO;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getRemetente() {
        return remetente;
    }

    public void setRemetente(String remetente) {
        this.remetente = remetente;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public boolean isSmtpStarttls() {
        return smtpStarttls;
    }

    public void setSmtpStarttls(boolean smtpStarttls) {
        this.smtpStarttls = smtpStarttls;
    }

    public boolean isSmtpDebug() {
        return smtpDebug;
    }

    public void setSmtpDebug(boolean smtpDebug) {
        this.smtpDebug = smtpDebug;
    }
}
