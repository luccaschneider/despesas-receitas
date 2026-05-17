package com.univates.despesasreceitas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração de mensageria por e-mail.
 * Ative com {@code app.email.enabled=true} e preencha {@code app.email.smtp.host} (e credenciais, se necessário).
 */
@ConfigurationProperties(prefix = "app.email")
public class AppEmailProperties {

    private boolean enabled = false;

    /** Endereço que recebe notificações de lançamentos */
    private String destinatario = "admin@email.com";

    /** Remetente (opcional). Se vazio, tenta-se usar o usuário SMTP quando for um e-mail. */
    private String remetente = "";

    private final Smtp smtp = new Smtp();

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

    public Smtp getSmtp() {
        return smtp;
    }

    public static class Smtp {
        private String host = "";
        private int port = 587;
        private String username = "";
        private String password = "";
        private boolean auth = true;
        private boolean starttls = true;
        /** Loga protocolo SMTP no stderr (útil para diagnóstico) */
        private boolean debug = false;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAuth() {
            return auth;
        }

        public void setAuth(boolean auth) {
            this.auth = auth;
        }

        public boolean isStarttls() {
            return starttls;
        }

        public void setStarttls(boolean starttls) {
            this.starttls = starttls;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }
    }
}
