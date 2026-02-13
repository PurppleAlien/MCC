package com.example.demo.ordenes.domain;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Embeddable;

/**
 * Representa el identificador único e inmutable de una Orden dentro del
 * dominio.
 */
@Embeddable
public final class OrdenId {

    /**
     * El valor único universal del identificador.
     */
    private final UUID valor;

    /**
     * Constructor protegido para uso exclusivo de frameworks de persistencia
     * (JPA/Hibernate).
     */
    protected OrdenId() {
        this.valor = null;
    }

    /**
     * Crea una instancia de OrdenId a partir de un UUID existente.
     * 
     * @param valor El UUID que se asignará al identificador.
     * @throws IllegalArgumentException si el valor es nulo.
     */
    public OrdenId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del ID no puede ser nulo");
        }
        this.valor = valor;
    }

    /**
     * Genera un nuevo OrdenId con un valor aleatorio (UUID v4).
     * 
     * @return Una nueva instancia de OrdenId.
     */
    public static OrdenId generar() {
        return new OrdenId(UUID.randomUUID());
    }

    /**
     * Obtiene el valor UUID contenido en este identificador.
     * 
     * @return El UUID de la orden.
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
        OrdenId ordenId = (OrdenId) o;
        return Objects.equals(valor, ordenId.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    /**
     * Retorna la representación en cadena de texto del identificador.
     * 
     * @return El UUID como String.
     */
    @Override
    public String toString() {
        return valor != null ? valor.toString() : "null";
    }
}
