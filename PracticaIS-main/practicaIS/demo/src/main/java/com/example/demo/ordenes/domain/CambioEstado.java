package com.example.demo.ordenes.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/**
 * Registro de auditoría de cada transición de estado.
 * Captura estado anterior, nuevo, fecha, motivo y usuario responsable.
 * Es un Value Object: inmutable y definido por sus atributos.
 */
@Embeddable
public final class CambioEstado {
    private final EstadoOrden estadoAnterior;
    private final EstadoOrden estadoNuevo;
    private final LocalDateTime fecha;
    private final String motivo;
    private final String usuario;

    // Constructor para JPA/Hibernate
    protected CambioEstado() {
        this.estadoAnterior = null;
        this.estadoNuevo = null;
        this.fecha = null;
        this.motivo = null;
        this.usuario = null;
    }

    public CambioEstado(EstadoOrden estadoAnterior, EstadoOrden estadoNuevo, LocalDateTime fecha, String motivo,
            String usuario) {
        if (estadoNuevo == null) {
            throw new IllegalArgumentException("El estado nuevo no puede ser nulo");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = fecha;
        this.motivo = motivo;
        this.usuario = usuario;
    }

    public static CambioEstado de(EstadoOrden anterior, EstadoOrden nuevo, String motivo, String usuario) {
        return new CambioEstado(anterior, nuevo, LocalDateTime.now(), motivo, usuario);
    }

    public EstadoOrden getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoOrden getEstadoNuevo() {
        return estadoNuevo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CambioEstado that = (CambioEstado) o;
        return estadoAnterior == that.estadoAnterior &&
                estadoNuevo == that.estadoNuevo &&
                Objects.equals(fecha, that.fecha) &&
                Objects.equals(motivo, that.motivo) &&
                Objects.equals(usuario, that.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estadoAnterior, estadoNuevo, fecha, motivo, usuario);
    }

    @Override
    public String toString() {
        return "CambioEstado{" +
                "anterior=" + estadoAnterior +
                ", nuevo=" + estadoNuevo +
                ", fecha=" + fecha +
                ", motivo='" + motivo + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
