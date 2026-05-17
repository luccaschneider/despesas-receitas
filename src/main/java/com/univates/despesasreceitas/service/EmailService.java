package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.entity.Lancamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ConfiguracaoEmailService configuracaoEmailService;

    public EmailService(ConfiguracaoEmailService configuracaoEmailService) {
        this.configuracaoEmailService = configuracaoEmailService;
    }

    @Async
    public void enviarEmailCriacao(Lancamento lancamento) {
        log.info(
            "Notificação por e-mail disparada (novo lançamento id={}, descricao={})",
            lancamento.getId(),
            lancamento.getDescricao()
        );
        enviarCorpo(
            "Novo Lançamento Criado",
            String.format(
                "Lançamento criado:\nID: %s\nDescrição: %s\nValor: R$ %.2f\nTipo: %s\nSituação: %s",
                lancamento.getId() != null ? lancamento.getId().toString() : "(novo)",
                lancamento.getDescricao(),
                lancamento.getValor(),
                lancamento.getTipoLancamento(),
                lancamento.getSituacao()
            ),
            false
        );
    }

    @Async
    public void enviarEmailAtualizacao(Lancamento lancamento) {
        log.info("Notificação por e-mail disparada (atualização id={})", lancamento.getId());
        enviarCorpo(
            "Lançamento Atualizado",
            String.format(
                "Lançamento atualizado:\nID: %d\nDescrição: %s\nValor: R$ %.2f\nTipo: %s\nSituação: %s",
                lancamento.getId(),
                lancamento.getDescricao(),
                lancamento.getValor(),
                lancamento.getTipoLancamento(),
                lancamento.getSituacao()
            ),
            false
        );
    }

    /**
     * Envio síncrono para feedback imediato na tela de administração.
     */
    public void enviarEmailTesteSincrono() {
        enviarCorpo(
            "Teste de configuração SMTP",
            "Este é um e-mail de teste enviado pela tela de configurações (administração).",
            true
        );
    }

    private void enviarCorpo(String assunto, String corpo, boolean propagarErro) {
        EffectiveEmailConfig cfg = configuracaoEmailService.resolverConfiguracaoEfetiva();

        if (!cfg.enabled()) {
            log.info(
                "E-mail não enviado: mensageria desligada na configuração efetiva "
                    + "(banco ou application.properties / variáveis de ambiente)."
            );
            if (propagarErro) {
                throw new IllegalStateException(
                    "E-mail está desligado. Ative \"Enviar notificações\" e salve antes de testar."
                );
            }
            return;
        }
        JavaMailSender mailSender = configuracaoEmailService.criarMailSender(cfg);
        if (mailSender == null) {
            log.warn(
                "E-mail não enviado: host SMTP ausente ou inválido na configuração efetiva."
            );
            if (propagarErro) {
                throw new IllegalStateException(
                    "Host SMTP não configurado. Informe o host (ex.: smtp.gmail.com) e salve."
                );
            }
            return;
        }
        if (!StringUtils.hasText(cfg.destinatario())) {
            log.warn("E-mail não enviado: destinatário está vazio.");
            if (propagarErro) {
                throw new IllegalStateException("Destinatário das notificações está vazio.");
            }
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(cfg.destinatario().trim());
            msg.setSubject("[Despesas & Receitas] " + assunto);
            msg.setText(corpo);
            definirRemetente(msg, cfg);

            log.info(
                "Enviando e-mail SMTP: assunto='{}', para={}, host={}",
                msg.getSubject(),
                cfg.destinatario(),
                cfg.smtpHost()
            );
            mailSender.send(msg);
            log.info(
                "E-mail enviado com sucesso: assunto='{}', destinatario={}",
                assunto,
                cfg.destinatario()
            );
        } catch (Exception e) {
            log.error(
                "Falha ao enviar e-mail (assunto='{}', destinatario={}): {} — {}",
                assunto,
                cfg.destinatario(),
                e.getClass().getSimpleName(),
                e.getMessage()
            );
            log.debug("Detalhes da falha de e-mail", e);
            if (propagarErro) {
                throw new IllegalStateException("Falha ao enviar e-mail: " + e.getMessage(), e);
            }
        }
    }

    private void definirRemetente(SimpleMailMessage msg, EffectiveEmailConfig cfg) {
        if (StringUtils.hasText(cfg.remetente())) {
            msg.setFrom(cfg.remetente().trim());
            return;
        }
        String user = cfg.smtpUsername();
        if (StringUtils.hasText(user) && user.contains("@")) {
            msg.setFrom(user.trim());
        }
    }
}
