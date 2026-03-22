package com.example.demo.ordenes.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/**
 * Información del envío una vez que la orden ha sido despachada.
 * Contiene datos del proveedor logístico y número de guía para rastreo.
 * Es inmutable para mantener el historial de despacho.
 */
@Embeddable
public final class InfoEnvio {

    private final String proveedorLogistico;
    private final String numeroGuia;
    private final LocalDateTime fechaEstimadaEntrega;

    /**
     * Constructor protegido para frameworks de persistencia.
     */
    protected InfoEnvio() {
        this.proveedorLogistico = null;
        this.numeroGuia = null;
        this.fechaEstimadaEntrega = null;
    }

    /**
     * Crea una nueva instancia de InfoEnvio.
     * 
     * @param proveedorLogistico   Nombre del proveedor (ej. "FedEx", "DHL").
     * @param numeroGuia           Número de guía o tracking ID.
     * @param fechaEstimadaEntrega Fecha tentativa de llegada.
     * @throws IllegalArgumentException si los campos obligatorios son nulos.
     */
    public InfoEnvio(String proveedorLogistico, String numeroGuia, LocalDateTime fechaEstimadaEntrega) {
        if (proveedorLogistico == null || proveedorLogistico.trim().isEmpty()) {
            throw new IllegalArgumentException("El proveedor logístico no puede estar vacío");
        }
        if (numeroGuia == null || numeroGuia.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de guía no puede estar vacío");
        }
        if (fechaEstimadaEntrega == null) {
            throw new IllegalArgumentException("La fecha estimada de entrega no puede ser nula");
        }

        this.proveedorLogistico = proveedorLogistico;
        this.numeroGuia = numeroGuia;
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
    }

    /**
     * Genera una URL de rastreo simulada basada en el proveedor.
     * 
     * @return URL de rastreo.
     */
    public String generarUrlRastreo() {
        String base = "https://rastreo.ejemplo.com/";
        String prov = proveedorLogistico.toLowerCase();

        if (prov.contains("fedex"))
            base = "https://www.fedex.com/track?tracknumbers=";
        else if (prov.contains("dhl"))
            base = "https://www.dhl.com/track?tracking_id=";
        else if (prov.contains("estafeta"))
            base = "https://www.estafeta.com/rastreo?guia=";

        return base + numeroGuia;
    }

    // Getters
    public String getProveedorLogistico() {
        return proveedorLogistico;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public LocalDateTime getFechaEstimadaEntrega() {
        return fechaEstimadaEntrega;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InfoEnvio infoEnvio = (InfoEnvio) o;
        return Objects.equals(proveedorLogistico, infoEnvio.proveedorLogistico) &&
                Objects.equals(numeroGuia, infoEnvio.numeroGuia) &&
                Objects.equals(fechaEstimadaEntrega, infoEnvio.fechaEstimadaEntrega);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proveedorLogistico, numeroGuia, fechaEstimadaEntrega);
    }

    @Override
    public String toString() {
        return "InfoEnvio{" +
                "proveedor='" + proveedorLogistico + '\'' +
                ", guia='" + numeroGuia + '\'' +
                ", entrega=" + fechaEstimadaEntrega +
                '}';
    }
}
