package com.example.demo.ventas.domain;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import java.util.Objects;

@Embeddable
public final class ItemCarritoId {
    private final UUID valor;

    protected ItemCarritoId() {
        this.valor = null;
    }

    public ItemCarritoId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El ID del item de carrito no puede ser nulo");
        }
        this.valor = valor;
    }

    public static ItemCarritoId generar() {
        return new ItemCarritoId(UUID.randomUUID());
    }

    public UUID getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCarritoId that = (ItemCarritoId) o;
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