package com.example.demo.shared.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public final class ClienteId {

    private final UUID valor;

    protected ClienteId() {
        this.valor = null;
    }

    @JsonCreator
    public ClienteId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del ID de cliente no puede ser nulo");
        }
        this.valor = valor;
    }

    public static ClienteId of(String id) {
        try {
            return new ClienteId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El formato del ID de cliente no es un UUID válido: " + id);
        }
    }

    @JsonValue
    public UUID getValue() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteId clienteId = (ClienteId) o;
        return Objects.equals(valor, clienteId.valor);
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