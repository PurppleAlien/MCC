package com.example.demo.catalogo.domain;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.UUID;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto_estadisticas")
public class Productoestadisticas {
     @Id
   @Column(columnDefinition = "VARBINARY(16)")
   private UUID productoId;
   private long ventasTotales;           // número de transacciones
   private long cantidadVendida;         // unidades vendidas
   private long vecesAgregadoAlCarrito;
   private Instant ultimaVentaAt;
   private Instant ultimaAgregadoAlCarritoAt;

   public Productoestadisticas(UUID productoId, long ventasTotales,long cantidadVendida,long vecesAgregadoAlCarrito,Instant ultimaVentaAt,Instant ultimaAgregadoAlCarritoAt){
         this.productoId= productoId;
         this.ventasTotales=ventasTotales;
         this.cantidadVendida=cantidadVendida;
         this.vecesAgregadoAlCarrito= vecesAgregadoAlCarrito;
         this.ultimaVentaAt=ultimaVentaAt;
         this.ultimaAgregadoAlCarritoAt=ultimaAgregadoAlCarritoAt;
   }

   public Productoestadisticas() {}
     
  public UUID getProductoId() {
        return productoId;
    }

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public long getVentasTotales() {
        return ventasTotales;
    }

    public void setVentasTotales(long ventasTotales) {
        this.ventasTotales = ventasTotales;
    }

    public long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public long getVecesAgregadoAlCarrito() {
        return vecesAgregadoAlCarrito;
    }

    public void setVecesAgregadoAlCarrito(long vecesAgregadoAlCarrito) {
        this.vecesAgregadoAlCarrito = vecesAgregadoAlCarrito;
    }

    public Instant getUltimaVentaAt() {
        return ultimaVentaAt;
    }

    public void setUltimaVentaAt(Instant ultimaVentaAt) {
        this.ultimaVentaAt = ultimaVentaAt;
    }

    public Instant getUltimaAgregadoAlCarritoAt() {
        return ultimaAgregadoAlCarritoAt;
    }

    public void setUltimaAgregadoAlCarritoAt(Instant ultimaAgregadoAlCarritoAt) {
        this.ultimaAgregadoAlCarritoAt = ultimaAgregadoAlCarritoAt;
    }

}
