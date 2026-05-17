package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.config.AppEmailProperties;
import com.univates.despesasreceitas.entity.ConfiguracaoEmail;
import com.univates.despesasreceitas.repository.ConfiguracaoEmailRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Service
public class ConfiguracaoEmailService {

    private final ConfiguracaoEmailRepository repository;
    private final AppEmailProperties appEmailProperties;

    public ConfiguracaoEmailService(
        ConfiguracaoEmailRepository repository,
        AppEmailProperties appEmailProperties
    ) {
        this.repository = repository;
        this.appEmailProperties = appEmailProperties;
    }

    @Transactional
    public ConfiguracaoEmail carregar() {
        return repository.findById(ConfiguracaoEmail.ID_FIXO).orElseGet(() -> {
            ConfiguracaoEmail novo = copiarDeAppProperties();
            novo.setId(ConfiguracaoEmail.ID_FIXO);
            return repository.save(novo);
        });
    }

    /**
     * Persiste alterações. Senha SMTP em branco mantém o valor já salvo.
     */
    @Transactional
    public ConfiguracaoEmail salvar(ConfiguracaoEmail formulario) {
        ConfiguracaoEmail existente = repository.findById(ConfiguracaoEmail.ID_FIXO).orElseGet(() -> {
            ConfiguracaoEmail n = copiarDeAppProperties();
            n.setId(ConfiguracaoEmail.ID_FIXO);
            return n;
        });

        existente.setEnabled(formulario.isEnabled());
        existente.setDestinatario(formulario.getDestinatario() != null ? formulario.getDestinatario().trim() : "");
        existente.setRemetente(formulario.getRemetente() != null ? formulario.getRemetente().trim() : "");
        existente.setSmtpHost(formulario.getSmtpHost() != null ? formulario.getSmtpHost().trim() : "");
        existente.setSmtpPort(formulario.getSmtpPort() > 0 ? formulario.getSmtpPort() : 587);
        existente.setSmtpUsername(formulario.getSmtpUsername() != null ? formulario.getSmtpUsername().trim() : "");
        existente.setSmtpAuth(formulario.isSmtpAuth());
        existente.setSmtpStarttls(formulario.isSmtpStarttls());
        existente.setSmtpDebug(formulario.isSmtpDebug());

        if (StringUtils.hasText(formulario.getSmtpPassword())) {
            existente.setSmtpPassword(formulario.getSmtpPassword());
        }

        return repository.save(existente);
    }

    /**
     * Banco tem precedência quando {@code enabled=true} e host SMTP não vazio.
     */
    public EffectiveEmailConfig resolverConfiguracaoEfetiva() {
        return repository.findById(ConfiguracaoEmail.ID_FIXO)
            .filter(db -> db.isEnabled() && StringUtils.hasText(db.getSmtpHost()))
            .map(EffectiveEmailConfig::fromDatabase)
            .orElse(EffectiveEmailConfig.fromAppProperties(appEmailProperties));
    }

    /**
     * Cria um {@link JavaMailSender} para a configuração efetiva, ou {@code null} se não for possível enviar.
     */
    public JavaMailSender criarMailSender(EffectiveEmailConfig cfg) {
        if (!cfg.enabled() || !StringUtils.hasText(cfg.smtpHost())) {
            return null;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg.smtpHost().trim());
        sender.setPort(cfg.smtpPort());
        if (StringUtils.hasText(cfg.smtpUsername())) {
            sender.setUsername(cfg.smtpUsername().trim());
        }
        if (StringUtils.hasText(cfg.smtpPassword())) {
            sender.setPassword(cfg.smtpPassword());
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(cfg.smtpAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(cfg.smtpStarttls()));
        props.put("mail.debug", String.valueOf(cfg.smtpDebug()));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }

    private ConfiguracaoEmail copiarDeAppProperties() {
        ConfiguracaoEmail c = new ConfiguracaoEmail();
        c.setId(ConfiguracaoEmail.ID_FIXO);
        c.setEnabled(appEmailProperties.isEnabled());
        c.setDestinatario(
            appEmailProperties.getDestinatario() != null ? appEmailProperties.getDestinatario() : ""
        );
        c.setRemetente(
            appEmailProperties.getRemetente() != null ? appEmailProperties.getRemetente() : ""
        );
        AppEmailProperties.Smtp s = appEmailProperties.getSmtp();
        c.setSmtpHost(s.getHost() != null ? s.getHost() : "");
        c.setSmtpPort(s.getPort());
        c.setSmtpUsername(s.getUsername() != null ? s.getUsername() : "");
        c.setSmtpPassword(s.getPassword() != null ? s.getPassword() : "");
        c.setSmtpAuth(s.isAuth());
        c.setSmtpStarttls(s.isStarttls());
        c.setSmtpDebug(s.isDebug());
        return c;
    }
}
