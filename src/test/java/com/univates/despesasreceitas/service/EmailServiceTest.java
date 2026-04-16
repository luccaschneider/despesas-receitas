package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.entity.Lancamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailService emailService;

    private Lancamento criarLancamento() {
        return new Lancamento(1L, "Salário", LocalDate.now(), new BigDecimal("5000.00"),
            Lancamento.TipoLancamento.RECEITA, Lancamento.Situacao.PAGO);
    }

    @Test void test16_enviarEmailCriacao_naoLancaException() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailCriacao(criarLancamento()));
    }

    @Test void test17_enviarEmailAtualizacao_naoLancaException() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailAtualizacao(criarLancamento()));
    }

    @Test void test18_enviarEmail_falhaSmtp_naoLancaException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailCriacao(criarLancamento()));
    }

    @Test void test19_enviarEmailAtualizacao_falhaSmtp_naoLancaException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailAtualizacao(criarLancamento()));
    }

    @Test void test20_enviarEmailCriacao_lancamentoDespesa() {
        Lancamento l = new Lancamento(2L, "Aluguel", LocalDate.now(), new BigDecimal("1200.00"),
            Lancamento.TipoLancamento.DESPESA, Lancamento.Situacao.PENDENTE);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailCriacao(l));
    }
}
