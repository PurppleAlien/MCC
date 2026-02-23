package com.example.demo.ordenes.dto;

import com.example.demo.ordenes.domain.*;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.Money;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrdenResponse {
    private OrdenId id;
    private String numeroOrden;
    private ClienteId clienteId;
    private List<ItemOrdenResponse> items;
    private DireccionEnvio direccionEnvio;
    private ResumenPago resumenPago;
    private InfoEnvio infoEnvio;
    private Money subtotal;
    private Money descuento;
    private Money total;
    private EstadoOrden estado;
    private LocalDateTime fechaCreacion;
    private List<CambioEstado> historialEstados;

    public static OrdenResponse fromOrden(Orden orden) {
        OrdenResponse response = new OrdenResponse();
        response.id = orden.getId();
        response.numeroOrden = orden.getNumeroOrden();
        response.clienteId = orden.getClienteId();
        response.items = orden.getItems().stream()
                .map(ItemOrdenResponse::fromItemOrden)
                .collect(Collectors.toList());
        response.direccionEnvio = orden.getDireccionEnvio();
        response.resumenPago = orden.getResumenPago();
        response.infoEnvio = orden.getInfoEnvio();
        response.subtotal = orden.getSubtotal();
        response.descuento = orden.getDescuento();
        response.total = orden.getTotal();
        response.estado = orden.getEstado();
        response.fechaCreacion = orden.getFechaCreacion();
        response.historialEstados = orden.getHistorialEstados();
        return response;
    }

    // Getters y setters
    public OrdenId getId() { return id; }
    public void setId(OrdenId id) { this.id = id; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    public ClienteId getClienteId() { return clienteId; }
    public void setClienteId(ClienteId clienteId) { this.clienteId = clienteId; }
    public List<ItemOrdenResponse> getItems() { return items; }
    public void setItems(List<ItemOrdenResponse> items) { this.items = items; }
    public DireccionEnvio getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(DireccionEnvio direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    public ResumenPago getResumenPago() { return resumenPago; }
    public void setResumenPago(ResumenPago resumenPago) { this.resumenPago = resumenPago; }
    public InfoEnvio getInfoEnvio() { return infoEnvio; }
    public void setInfoEnvio(InfoEnvio infoEnvio) { this.infoEnvio = infoEnvio; }
    public Money getSubtotal() { return subtotal; }
    public void setSubtotal(Money subtotal) { this.subtotal = subtotal; }
    public Money getDescuento() { return descuento; }
    public void setDescuento(Money descuento) { this.descuento = descuento; }
    public Money getTotal() { return total; }
    public void setTotal(Money total) { this.total = total; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<CambioEstado> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<CambioEstado> historialEstados) { this.historialEstados = historialEstados; }
}