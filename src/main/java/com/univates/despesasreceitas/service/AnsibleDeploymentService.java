package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.config.AnsibleDeployProperties;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnsibleDeploymentService {

    private static final Logger log = LoggerFactory.getLogger(AnsibleDeploymentService.class);
    private static final Pattern STATUS_PATTERN = Pattern.compile("ENV_STATUS=([a-zA-Z_]+)");

    private final AnsibleDeployProperties properties;

    public AnsibleDeploymentService(AnsibleDeployProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public List<EnvironmentView> listEnvironments() {
        return properties.getEnvironments().stream()
            .map(environment -> {
                EnvironmentStatus status = getStatus(environment.getId());
                return new EnvironmentView(
                    environment.getId(),
                    environment.getName(),
                    environment.getUrl(),
                    status,
                    status.getLabel()
                );
            })
            .toList();
    }

    public AnsibleCommandResult deploy(String environmentId) {
        ensureConfigured(environmentId);
        return runPlaybook(properties.getDeployPlaybook(), environmentId);
    }

    public AnsibleCommandResult stop(String environmentId) {
        ensureConfigured(environmentId);
        return runPlaybook(properties.getStopPlaybook(), environmentId);
    }

    private EnvironmentStatus getStatus(String environmentId) {
        if (!properties.isEnabled()) {
            return EnvironmentStatus.DISABLED;
        }

        try {
            AnsibleCommandResult result = runPlaybook(properties.getStatusPlaybook(), environmentId);
            if (!result.success()) {
                log.warn("Falha ao consultar status do ambiente {}: {}", environmentId, result.output());
                return EnvironmentStatus.UNKNOWN;
            }

            return parseStatus(result.output()).orElse(EnvironmentStatus.UNKNOWN);
        } catch (RuntimeException e) {
            log.warn("Erro ao consultar status do ambiente {}", environmentId, e);
            return EnvironmentStatus.UNKNOWN;
        }
    }

    private Optional<EnvironmentStatus> parseStatus(String output) {
        Matcher matcher = STATUS_PATTERN.matcher(output);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return switch (matcher.group(1).toLowerCase(Locale.ROOT)) {
            case "running" -> Optional.of(EnvironmentStatus.RUNNING);
            case "stopped" -> Optional.of(EnvironmentStatus.STOPPED);
            default -> Optional.of(EnvironmentStatus.UNKNOWN);
        };
    }

    private void ensureConfigured(String environmentId) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Execucao de Ansible desabilitada em app.deploy.ansible.enabled.");
        }

        properties.getEnvironments().stream()
            .filter(environment -> environmentId.equals(environment.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ambiente desconhecido: " + environmentId));
    }

    private AnsibleCommandResult runPlaybook(String playbook, String environmentId) {
        Path outputFile = null;
        try {
            outputFile = Files.createTempFile("ansible-" + environmentId + "-", ".log");
            ProcessBuilder processBuilder = new ProcessBuilder(
                properties.getExecutable(),
                "-i",
                properties.getInventory(),
                playbook,
                "-l",
                environmentId
            );
            processBuilder.directory(new File(properties.getWorkingDirectory()));
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(outputFile.toFile());

            Process process = processBuilder.start();
            boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException(
                    "Tempo limite excedido ao executar Ansible (" + Duration.ofSeconds(properties.getTimeoutSeconds()) + ")."
                );
            }

            String output = Files.readString(outputFile, StandardCharsets.UTF_8);
            int exitCode = process.exitValue();
            return new AnsibleCommandResult(exitCode == 0, exitCode, output);
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel iniciar o ansible-playbook: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execucao do Ansible interrompida.", e);
        } finally {
            if (outputFile != null) {
                try {
                    Files.deleteIfExists(outputFile);
                } catch (IOException e) {
                    log.debug("Nao foi possivel remover arquivo temporario {}", outputFile, e);
                }
            }
        }
    }
}
