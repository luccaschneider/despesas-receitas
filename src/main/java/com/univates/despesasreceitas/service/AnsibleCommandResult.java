package com.univates.despesasreceitas.service;

public record AnsibleCommandResult(
    boolean success,
    int exitCode,
    String output
) {
}
