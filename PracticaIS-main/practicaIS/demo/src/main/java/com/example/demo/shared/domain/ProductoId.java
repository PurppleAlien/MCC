package com.example.demo.shared.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class ProductoId {

    private final UUID valor;

    protected ProductoId() {
        this.valor = null;
    }

    @JsonCreator
    public ProductoId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        this.valor = valor;
    }

    public static ProductoId generar() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId of(String id) {
        return new ProductoId(UUID.fromString(id));
    }

    @JsonValue
    public UUID getValue() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoId that = (ProductoId) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor != null ? valor.toString() : "null";
    }
}