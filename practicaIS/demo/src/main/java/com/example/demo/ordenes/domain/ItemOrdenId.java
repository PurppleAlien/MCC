package com.example.demo.ordenes.domain;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Embeddable;

/**
 * Identificador único del item dentro de la orden.
 */
@Embeddable
public final class ItemOrdenId {

    private final UUID valor;

    /**
     * Constructor protegido para frameworks de persistencia.
     */
    protected ItemOrdenId() {
        this.valor = null;
    }

    /**
     * Crea una instancia de ItemOrdenId a partir de un UUID.
     * 
     * @param valor El UUID del item.
     */
    public ItemOrdenId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del ID de item no puede ser nulo");
        }
        this.valor = valor;
    }

    /**
     * Genera un nuevo ItemOrdenId con un valor aleatorio.
     * 
     * @return Una nueva instancia de ItemOrdenId.
     */
    public static ItemOrdenId generar() {
        return new ItemOrdenId(UUID.randomUUID());
    }

    /**
     * Obtiene el valor UUID.
     * 
     * @return El UUID.
     */
    public UUID getValue() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemOrdenId that = (ItemOrdenId) o;
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
