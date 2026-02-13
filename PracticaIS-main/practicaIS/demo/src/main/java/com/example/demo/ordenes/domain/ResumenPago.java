package com.example.demo.ordenes.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Información resumida del pago asociado a la orden.
 * Contiene referencia al contexto de Pagos y estado sincronizado.
 * Es inmutable para asegurar la integridad del registro histórico.
 */
@Embeddable
public final class ResumenPago {

    private final String metodoPago;
    private final String referenciaExterna;

    @Enumerated(EnumType.STRING)
    private final EstadoPago estado;

    private final LocalDateTime fechaProcesamiento;

    /**
     * Constructor protegido para frameworks de persistencia.
     */
    protected ResumenPago() {
        this.metodoPago = null;
        this.referenciaExterna = null;
        this.estado = null;
        this.fechaProcesamiento = null;
    }

    /**
     * Crea una nueva instancia de ResumenPago.
     * 
     * @param metodoPago         El método de pago utilizado (ej. "Tarjeta de
     *                           Crédito").
     * @param referenciaExterna  El ID del pago en el sistema externo o contexto de
     *                           Pagos.
     * @param estado             El estado actual del pago.
     * @param fechaProcesamiento La fecha y hora en que se procesó el pago.
     * @throws IllegalArgumentException si los campos obligatorios son nulos.
     */
    public ResumenPago(String metodoPago, String referenciaExterna, EstadoPago estado,
            LocalDateTime fechaProcesamiento) {
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            throw new IllegalArgumentException("El método de pago no puede estar vacío");
        }
        if (referenciaExterna == null || referenciaExterna.trim().isEmpty()) {
            throw new IllegalArgumentException("La referencia externa de pago no puede estar vacía");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado de pago no puede ser nulo");
        }
        if (fechaProcesamiento == null) {
            throw new IllegalArgumentException("La fecha de procesamiento no puede ser nula");
        }

        this.metodoPago = metodoPago;
        this.referenciaExterna = referenciaExterna;
        this.estado = estado;
        this.fechaProcesamiento = fechaProcesamiento;
    }

    // Getters
    public String getMetodoPago() {
        return metodoPago;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResumenPago that = (ResumenPago) o;
        return Objects.equals(metodoPago, that.metodoPago) &&
                Objects.equals(referenciaExterna, that.referenciaExterna) &&
                estado == that.estado &&
                Objects.equals(fechaProcesamiento, that.fechaProcesamiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metodoPago, referenciaExterna, estado, fechaProcesamiento);
    }

    @Override
    public String toString() {
        return "ResumenPago{" +
                "metodo='" + metodoPago + '\'' +
                ", ref='" + referenciaExterna + '\'' +
                ", estado=" + estado +
                ", fecha=" + fechaProcesamiento +
                '}';
    }
}
