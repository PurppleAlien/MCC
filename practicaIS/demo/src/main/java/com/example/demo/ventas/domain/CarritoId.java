package com.example.demo.ventas.domain;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import java.util.Objects;

@Embeddable
public final class CarritoId {
    private final UUID valor;

    protected CarritoId() {
        this.valor = null;
    }

    public CarritoId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El ID del carrito no puede ser nulo");
        }
        this.valor = valor;
    }

    public static CarritoId generar() {
        return new CarritoId(UUID.randomUUID());
    }

    public UUID getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarritoId that = (CarritoId) o;
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