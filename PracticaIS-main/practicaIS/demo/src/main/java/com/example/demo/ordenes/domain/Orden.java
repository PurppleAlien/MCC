package com.example.demo.ordenes.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Representa una transacción de compra confirmada (Aggregate Root).
 * Gestiona el ciclo de vida completo y coordina items, envío y pago.
 */
@Entity
@Table(name = "ordenes")
public class Orden {

    @EmbeddedId
    @AttributeOverride(name = "valor", column = @Column(name = "id"))
    private OrdenId id;

    private String numeroOrden;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "cliente_id"))
    private ClienteId clienteId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orden_id")
    private List<ItemOrden> items = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "estado", column = @Column(name = "direccion_estado"))
    private DireccionEnvio direccionEnvio;

    @Embedded
    @AttributeOverride(name = "estado", column = @Column(name = "pago_estado"))
    private ResumenPago resumenPago;

    @Embedded
    private InfoEnvio infoEnvio;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cantidad", column = @Column(name = "subtotal_cantidad")),
            @AttributeOverride(name = "moneda", column = @Column(name = "subtotal_moneda"))
    })
    private Money subtotal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cantidad", column = @Column(name = "descuento_cantidad")),
            @AttributeOverride(name = "moneda", column = @Column(name = "descuento_moneda"))
    })
    private Money descuento;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cantidad", column = @Column(name = "total_cantidad")),
            @AttributeOverride(name = "moneda", column = @Column(name = "total_moneda"))
    })
    private Money total;

    @Enumerated(EnumType.STRING)
    private EstadoOrden estado;

    private LocalDateTime fechaCreacion;

    @ElementCollection
    @CollectionTable(name = "orden_historial_estados", joinColumns = @JoinColumn(name = "orden_id"))
    private List<CambioEstado> historialEstados = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     */
    protected Orden() {
    }

    /**
     * Crea una nueva orden (RN-ORD-01, RN-ORD-02).
     */
    public static Orden crear(OrdenId id, String numeroOrden, ClienteId clienteId,
            List<ItemOrden> items, DireccionEnvio direccion, Money descuento) {

        // RN-ORD-01: Una orden debe tener al menos un item
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Una orden debe tener al menos un item");
        }

        Orden orden = new Orden();
        orden.id = id;
        orden.numeroOrden = numeroOrden;
        orden.clienteId = clienteId;
        orden.items = new ArrayList<>(items);
        orden.direccionEnvio = direccion;
        orden.descuento = descuento;
        orden.estado = EstadoOrden.PENDIENTE;
        orden.fechaCreacion = LocalDateTime.now();

        orden.recalcularTotales();

        // RN-ORD-02: El total de la orden debe ser mayor a cero
        if (orden.total.getCantidad().doubleValue() <= 0) {
            throw new IllegalArgumentException("El total de la orden debe ser mayor a cero");
        }

        return orden;
    }

    private void recalcularTotales() {
        Money acumulado = Money.pesos(0);
        for (ItemOrden item : items) {
            acumulado = acumulado.sumar(item.getSubtotal());
        }
        this.subtotal = acumulado;
        this.total = subtotal.restar(descuento);
    }

    /**
     * Confirma la orden (RN-ORD-05, RN-ORD-06).
     */
    public void confirmar(String usuario) {
        if (this.estado != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una orden en estado PENDIENTE");
        }
        actualizarEstado(EstadoOrden.CONFIRMADA, "Orden confirmada por el usuario", usuario);
    }

    /**
     * Procesa el pago de la orden (RN-ORD-07, RN-ORD-08).
     */
    public void procesarPago(String metodo, String referenciaPago, String usuario) {
        if (this.estado != EstadoOrden.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede procesar pago si la orden está CONFIRMADA");
        }
        if (referenciaPago == null || referenciaPago.isBlank()) {
            throw new IllegalArgumentException("La referencia de pago no puede estar vacía");
        }

        this.resumenPago = new ResumenPago(metodo, referenciaPago, EstadoPago.APROBADO, LocalDateTime.now());
        actualizarEstado(EstadoOrden.PAGO_PROCESADO, "Pago aprobado y procesado", usuario);
    }

    /**
     * Marca la orden en proceso de preparación (RN-ORD-09).
     */
    public void marcarEnProceso(String usuario) {
        if (this.estado != EstadoOrden.PAGO_PROCESADO) {
            throw new IllegalStateException("Solo se puede marcar en proceso si el pago fue procesado");
        }
        actualizarEstado(EstadoOrden.EN_PREPARACION, "Orden en proceso de preparación logísitca", usuario);
    }

    /**
     * Marca la orden como enviada (RN-ORD-10, RN-ORD-11, RN-ORD-12).
     */
    public void marcarEnviada(InfoEnvio info, String usuario) {
        if (this.estado != EstadoOrden.EN_PREPARACION) {
            throw new IllegalStateException("Solo se puede marcar enviada si está EN_PREPARACION");
        }
        if (info == null || info.getNumeroGuia() == null) {
            throw new IllegalArgumentException("Debe proporcionarse el número de guía del envío");
        }
        if (info.getNumeroGuia().length() < 10) {
            throw new IllegalArgumentException("El número de guía debe tener al menos 10 caracteres");
        }

        this.infoEnvio = info;
        actualizarEstado(EstadoOrden.ENVIADA, "Pedido despachado y en camino", usuario);
    }

    /**
     * Marca la orden como entregada (RN-ORD-13).
     */
    public void marcarEntregada(String usuario) {
        if (this.estado != EstadoOrden.ENVIADA && this.estado != EstadoOrden.EN_TRANSITO) {
            throw new IllegalStateException("Solo se puede marcar entregada si está ENVIADA o EN_TRANSITO");
        }
        actualizarEstado(EstadoOrden.ENTREGADA, "Pedido entregado satisfactoriamente", usuario);
    }

    /**
     * Cancela la orden (RN-ORD-14, RN-ORD-15, RN-ORD-16).
     */
    public void cancelar(String motivo, String usuario) {
        if (this.estado == EstadoOrden.ENVIADA || this.estado == EstadoOrden.ENTREGADA) {
            throw new IllegalStateException("No se puede cancelar una orden ya ENVIADA o ENTREGADA");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionarse un motivo de cancelación");
        }
        if (motivo.length() < 10) {
            throw new IllegalArgumentException("El motivo de cancelación debe tener al menos 10 caracteres");
        }

        actualizarEstado(EstadoOrden.CANCELADA, motivo, usuario);
    }

    private void actualizarEstado(EstadoOrden nuevoEstado, String motivo, String usuario) {
        CambioEstado cambio = CambioEstado.de(this.estado, nuevoEstado, motivo, usuario);
        this.historialEstados.add(cambio);
        this.estado = nuevoEstado;
    }

    // Getters
    public OrdenId getId() {
        return id;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public ClienteId getClienteId() {
        return clienteId;
    }

    public List<ItemOrden> getItems() {
        return Collections.unmodifiableList(items);
    }

    public DireccionEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public ResumenPago getResumenPago() {
        return resumenPago;
    }

    public InfoEnvio getInfoEnvio() {
        return infoEnvio;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getDescuento() {
        return descuento;
    }

    public Money getTotal() {
        return total;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public List<CambioEstado> getHistorialEstados() {
        return Collections.unmodifiableList(historialEstados);
    }

    public EstadoOrden obtenerEstadoActual() {
        return estado;
    }

    public List<CambioEstado> obtenerHistorial() {
        return getHistorialEstados();
    }
}
