package com.example.demo.ordenes.domain;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Embeddable;

/**
 * Referencia al cliente que realizó la compra.
 */
@Embeddable
public final class ClienteId {

    private final UUID valor;

    /**
     * Constructor protegido para frameworks de persistencia.
     */
    protected ClienteId() {
        this.valor = null;
    }

    /**
     * Crea una instancia de ClienteId a partir de un UUID.
     * 
     * @param valor El UUID del cliente.
     * @throws IllegalArgumentException si el valor es nulo.
     */
    public ClienteId(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor del ID de cliente no puede ser nulo");
        }
        this.valor = valor;
    }

    /**
     * Método de fábrica para crear un ClienteId a partir de una cadena de texto
     * (String).
     * 
     * @param id La cadena que representa el UUID.
     * @return Una nueva instancia de ClienteId.
     * @throws IllegalArgumentException si el formato de la cadena no es un UUID
     *                                  válido.
     */
    public static ClienteId of(String id) {
        try {
            return new ClienteId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El formato del ID de cliente no es un UUID válido: " + id);
        }
    }

    /**
     * Obtiene el valor UUID del cliente.
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
