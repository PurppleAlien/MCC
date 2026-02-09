package com.example.demo.catalogo.domain;

import java.util.UUID;
import jakarta.persistence.Embeddable;

@Embeddable
public record ProductoId(UUID valor) {

    public ProductoId {
        if (valor == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
    }

    public static ProductoId generar() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId of(String id) {
        return new ProductoId(UUID.fromString(id));
    }

    public UUID getValue() {
        return valor;
    }
}