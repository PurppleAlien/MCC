package com.uamishop.catalogo.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "producto_estadisticas")
public class ProductoEstadisticas {

    @Id
    @Column(columnDefinition = "VARBINARY(16)")
    private UUID productoId;

    private long ventasTotales;           // número de transacciones
    private long cantidadVendida;         // unidades vendidas
    private long vecesAgregadoAlCarrito;
    private Instant ultimaVentaAt;
    private Instant ultimaAgregadoAlCarritoAt;

    protected ProductoEstadisticas() {}

    public ProductoEstadisticas(UUID productoId) {
        this.productoId = productoId;
        this.ventasTotales = 0;
        this.cantidadVendida = 0;
        this.vecesAgregadoAlCarrito = 0;
    }

    // Getters y setters
    public UUID getProductoId() { return productoId; }
    public long getVentasTotales() { return ventasTotales; }
    public long getCantidadVendida() { return cantidadVendida; }
    public long getVecesAgregadoAlCarrito() { return vecesAgregadoAlCarrito; }
    public Instant getUltimaVentaAt() { return ultimaVentaAt; }
    public Instant getUltimaAgregadoAlCarritoAt() { return ultimaAgregadoAlCarritoAt; }

    public void registrarVenta(int cantidad, Instant momento) {
        this.ventasTotales++;
        this.cantidadVendida += cantidad;
        this.ultimaVentaAt = momento;
    }

    public void registrarAgregadoAlCarrito(Instant momento) {
        this.vecesAgregadoAlCarrito++;
        this.ultimaAgregadoAlCarritoAt = momento;
    }
}