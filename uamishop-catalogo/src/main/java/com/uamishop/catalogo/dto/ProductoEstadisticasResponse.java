package com.uamishop.catalogo.dto;

import com.uamishop.catalogo.domain.ProductoEstadisticas;
import java.time.Instant;
import java.util.UUID;

public record ProductoEstadisticasResponse(
    UUID productoId,
    long ventasTotales,
    long cantidadVendida,
    long vecesAgregadoAlCarrito,
    Instant ultimaVentaAt
) {
    public static ProductoEstadisticasResponse fromEntity(ProductoEstadisticas stats) {
        return new ProductoEstadisticasResponse(
            stats.getProductoId(),
            stats.getVentasTotales(),
            stats.getCantidadVendida(),
            stats.getVecesAgregadoAlCarrito(),
            stats.getUltimaVentaAt()
        );
    }
}