package com.example.demo.ordenes.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa el identificador único e inmutable de una Orden dentro del
 * dominio.
 */
@Embeddable
public final class OrdenId {

    private final UUID valor;

    protected OrdenId() {
        this.valor = null;
    }

    @JsonCreator
    public OrdenId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del ID no puede ser nulo");
        }
        this.valor = valor;
    }

    public static OrdenId generar() {
        return new OrdenId(UUID.randomUUID());
    }

    @JsonValue
    public UUID getValue() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenId ordenId = (OrdenId) o;
        return Objects.equals(valor, ordenId.valor);
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