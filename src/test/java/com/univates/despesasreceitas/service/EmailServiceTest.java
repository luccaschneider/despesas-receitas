package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.config.AppEmailProperties;
import com.univates.despesasreceitas.entity.ConfiguracaoEmail;
import com.univates.despesasreceitas.repository.ConfiguracaoEmailRepository;
import com.univates.despesasreceitas.entity.Lancamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ConfiguracaoEmailRepository configuracaoEmailRepository;

    private AppEmailProperties appEmailProperties;

    private EmailService emailService;

    private ConfiguracaoEmail configAtivaNoBanco() {
        ConfiguracaoEmail c = new ConfiguracaoEmail();
        c.setId(ConfiguracaoEmail.ID_FIXO);
        c.setEnabled(true);
        c.setDestinatario("notificado@example.com");
        c.setRemetente("");
        c.setSmtpHost("smtp.example.com");
        c.setSmtpPort(587);
        c.setSmtpUsername("user@example.com");
        c.setSmtpPassword("secret");
        c.setSmtpAuth(true);
        c.setSmtpStarttls(true);
        c.setSmtpDebug(false);
        return c;
    }

    /** Evita mockar {@link ConfiguracaoEmailService} (problemas com Mockito / Byte Buddy em JVMs mais novas). */
    private static final class ConfiguracaoEmailServiceTeste extends ConfiguracaoEmailService {
        private final JavaMailSender mailSenderFixo;

        private ConfiguracaoEmailServiceTeste(
            ConfiguracaoEmailRepository repository,
            AppEmailProperties appEmailProperties,
            JavaMailSender mailSenderFixo
        ) {
            super(repository, appEmailProperties);
            this.mailSenderFixo = mailSenderFixo;
        }

        @Override
        public JavaMailSender criarMailSender(EffectiveEmailConfig cfg) {
            return mailSenderFixo;
        }
    }

    @BeforeEach
    void setUp() {
        appEmailProperties = new AppEmailProperties();
        when(configuracaoEmailRepository.findById(ConfiguracaoEmail.ID_FIXO))
            .thenReturn(Optional.of(configAtivaNoBanco()));
        ConfiguracaoEmailService cfg = new ConfiguracaoEmailServiceTeste(
            configuracaoEmailRepository,
            appEmailProperties,
            mailSender
        );
        emailService = new EmailService(cfg);
    }

    private Lancamento criarLancamento() {
        return new Lancamento(1L, "Salário", LocalDate.now(), new BigDecimal("5000.00"),
            Lancamento.TipoLancamento.RECEITA, Lancamento.Situacao.PAGO);
    }

    @Test
    void test16_enviarEmailCriacao_naoLancaException() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailCriacao(criarLancamento()));
    }

    @Test
    void test17_enviarEmailAtualizacao_naoLancaException() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailAtualizacao(criarLancamento()));
    }

    @Test
    void test18_enviarEmail_falhaSmtp_naoLancaException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailCriacao(criarLancamento()));
    }

    @Test
    void test19_enviarEmailAtualizacao_falhaSmtp_naoLancaException() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.enviarEmailAtualizacao(criarLancamento()));
    }

    @Test
    void test20_enviarEmailCriacao_lancamentoDespesa() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        Lancamento l = new Lancamento(2L, "Aluguel", LocalDate.now(), new BigDecimal("1200.00"),
            Lancamento.TipoLancamento.DESPESA, Lancamento.Situacao.PENDENTE);
        assertDoesNotThrow(() -> emailService.enviarEmailCriacao(l));
    }
}
