package com.univates.despesasreceitas.service;

public record EnvironmentView(
    String id,
    String name,
    String url,
    EnvironmentStatus status,
    String statusDetail
) {
}
