package com.univates.despesasreceitas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(AppEmailProperties.class)
public class EmailMessagingConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
    public JavaMailSender javaMailSender(AppEmailProperties emailProperties) {
        AppEmailProperties.Smtp smtp = emailProperties.getSmtp();
        if (!StringUtils.hasText(smtp.getHost())) {
            throw new IllegalStateException(
                "app.email.enabled=true, mas app.email.smtp.host está vazio. "
                    + "Defina host (e credenciais) em application.properties ou variáveis de ambiente."
            );
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtp.getHost().trim());
        sender.setPort(smtp.getPort());
        if (StringUtils.hasText(smtp.getUsername())) {
            sender.setUsername(smtp.getUsername().trim());
        }
        if (StringUtils.hasText(smtp.getPassword())) {
            sender.setPassword(smtp.getPassword());
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(smtp.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(smtp.isStarttls()));
        props.put("mail.debug", String.valueOf(smtp.isDebug()));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }
}
