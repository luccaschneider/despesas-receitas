package com.univates.despesasreceitas.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.deploy.ansible")
public class AnsibleDeployProperties {

    private boolean enabled = true;
    private String executable = "ansible-playbook";
    private String workingDirectory = ".";
    private String inventory = "ansible/inventory/hosts.yml";
    private String deployPlaybook = "ansible/playbooks/deploy.yml";
    private String stopPlaybook = "ansible/playbooks/stop.yml";
    private String statusPlaybook = "ansible/playbooks/status.yml";
    private int timeoutSeconds = 600;
    private List<Environment> environments = defaultEnvironments();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public String getDeployPlaybook() {
        return deployPlaybook;
    }

    public void setDeployPlaybook(String deployPlaybook) {
        this.deployPlaybook = deployPlaybook;
    }

    public String getStopPlaybook() {
        return stopPlaybook;
    }

    public void setStopPlaybook(String stopPlaybook) {
        this.stopPlaybook = stopPlaybook;
    }

    public String getStatusPlaybook() {
        return statusPlaybook;
    }

    public void setStatusPlaybook(String statusPlaybook) {
        this.statusPlaybook = statusPlaybook;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }

    private static List<Environment> defaultEnvironments() {
        List<Environment> defaults = new ArrayList<>();
        defaults.add(new Environment("homolog", "Homologacao", "http://localhost:8081"));
        defaults.add(new Environment("prod", "Producao", "http://localhost:8082"));
        return defaults;
    }

    public static class Environment {
        private String id;
        private String name;
        private String url;

        public Environment() {
        }

        public Environment(String id, String name, String url) {
            this.id = id;
            this.name = name;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
