package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.config.AppEmailProperties;
import com.univates.despesasreceitas.entity.ConfiguracaoEmail;

/**
 * Snapshot efetivo das configurações de e-mail após aplicar precedência (banco vs properties).
 */
public record EffectiveEmailConfig(
    boolean enabled,
    String destinatario,
    String remetente,
    String smtpHost,
    int smtpPort,
    String smtpUsername,
    String smtpPassword,
    boolean smtpAuth,
    boolean smtpStarttls,
    boolean smtpDebug
) {
    public static EffectiveEmailConfig fromDatabase(ConfiguracaoEmail c) {
        return new EffectiveEmailConfig(
            c.isEnabled(),
            c.getDestinatario(),
            c.getRemetente(),
            c.getSmtpHost(),
            c.getSmtpPort(),
            c.getSmtpUsername(),
            c.getSmtpPassword(),
            c.isSmtpAuth(),
            c.isSmtpStarttls(),
            c.isSmtpDebug()
        );
    }

    public static EffectiveEmailConfig fromAppProperties(AppEmailProperties p) {
        AppEmailProperties.Smtp s = p.getSmtp();
        return new EffectiveEmailConfig(
            p.isEnabled(),
            p.getDestinatario(),
            p.getRemetente(),
            s.getHost(),
            s.getPort(),
            s.getUsername(),
            s.getPassword(),
            s.isAuth(),
            s.isStarttls(),
            s.isDebug()
        );
    }
}
