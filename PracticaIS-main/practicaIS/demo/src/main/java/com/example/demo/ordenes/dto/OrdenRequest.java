package com.example.demo.ordenes.dto;

import com.example.demo.shared.domain.ClienteId;
import com.example.demo.ordenes.domain.DireccionEnvio;
import com.example.demo.shared.domain.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class OrdenRequest {

    @NotBlank(message = "El número de orden es obligatorio")
    private String numeroOrden;

    @NotNull(message = "El ID del cliente es obligatorio")
    private ClienteId clienteId;

    @Valid
    @NotNull(message = "La lista de items no puede ser nula")
    @Size(min = 1, message = "La orden debe tener al menos un item")
    private List<ItemOrdenRequest> items;

    @NotNull(message = "La dirección de envío es obligatoria")
    private DireccionEnvio direccionEnvio;

    private Money descuento; // opcional

    // Getters y setters
    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public ClienteId getClienteId() {
        return clienteId;
    }

    public void setClienteId(ClienteId clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemOrdenRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemOrdenRequest> items) {
        this.items = items;
    }

    public DireccionEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(DireccionEnvio direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public Money getDescuento() {
        return descuento;
    }

    public void setDescuento(Money descuento) {
        this.descuento = descuento;
    }
}