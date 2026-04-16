package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.entity.Lancamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.destinatario:admin@email.com}")
    private String destinatario;

    @Async
    public void enviarEmailCriacao(Lancamento lancamento) {
        enviar(
            "Novo Lançamento Criado",
            String.format("Lançamento criado:\nDescrição: %s\nValor: R$ %.2f\nTipo: %s\nSituação: %s",
                lancamento.getDescricao(), lancamento.getValor(),
                lancamento.getTipoLancamento(), lancamento.getSituacao())
        );
    }

    @Async
    public void enviarEmailAtualizacao(Lancamento lancamento) {
        enviar(
            "Lançamento Atualizado",
            String.format("Lançamento atualizado:\nID: %d\nDescrição: %s\nValor: R$ %.2f\nTipo: %s\nSituação: %s",
                lancamento.getId(), lancamento.getDescricao(), lancamento.getValor(),
                lancamento.getTipoLancamento(), lancamento.getSituacao())
        );
    }

    private void enviar(String assunto, String corpo) {
        try {
            if (mailSender == null) return;
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(destinatario);
            msg.setSubject("[Despesas & Receitas] " + assunto);
            msg.setText(corpo);
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println("Email não enviado (configure SMTP): " + e.getMessage());
        }
    }
}
