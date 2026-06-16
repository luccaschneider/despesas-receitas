package com.univates.despesasreceitas.service;

public enum EnvironmentStatus {
    RUNNING("Rodando", "running"),
    STOPPED("Parado", "stopped"),
    UNKNOWN("Desconhecido", "unknown"),
    DISABLED("Ansible desabilitado", "disabled");

    private final String label;
    private final String cssClass;

    EnvironmentStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }
}
