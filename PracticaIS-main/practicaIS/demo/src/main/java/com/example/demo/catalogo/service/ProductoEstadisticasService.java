package com.example.demo.catalogo.service;
import java.time.Instant;
import java.util.UUID;
import com.example.demo.catalogo.domain.Productoestadisticas;
import com.example.demo.catalogo.repository.ProductoEstadisticasJpaRepository;
import java.util.List;

public class ProductoEstadisticasService {
    private final ProductoEstadisticasJpaRepository repository;

    public ProductoEstadisticasService(ProductoEstadisticasJpaRepository repository) {
        this.repository = repository;
    }
    
 public void registrarVenta(UUID productoId, int cantidad){
    Productoestadisticas estadisticas = repository.findById(productoId).orElse(null);

        if (estadisticas == null) {

            estadisticas = new Productoestadisticas();
            estadisticas.setProductoId(productoId);
            estadisticas.setVentasTotales(0);
            estadisticas.setCantidadVendida(0);
            estadisticas.setVecesAgregadoAlCarrito(0);
            estadisticas.setUltimaVentaAt(null);
            estadisticas.setUltimaAgregadoAlCarritoAt(null);
        }

        
        estadisticas.setVentasTotales(estadisticas.getVentasTotales() + 1);
        estadisticas.setCantidadVendida(estadisticas.getCantidadVendida() + cantidad);
        estadisticas.setUltimaVentaAt(Instant.now());

        repository.save(estadisticas);
    }

    public void RegistrarAgregadosAlCarrito(UUID productoId){
     Productoestadisticas producto = repository.findById(productoId).orElse(null);

     if(producto==null){
        producto = new Productoestadisticas();
        producto.setProductoId(productoId);
        producto.setVentasTotales(0);
        producto.setCantidadVendida(0);
        producto.setVecesAgregadoAlCarrito(0);
        producto.setUltimaVentaAt(null);
        producto.setUltimaAgregadoAlCarritoAt(null);
     }
    
     producto.setVecesAgregadoAlCarrito(producto.getVecesAgregadoAlCarrito()+ 1);
     producto.setUltimaAgregadoAlCarritoAt(Instant.now());
 
     repository.save(producto);
    }

    public List<Productoestadisticas> obtenerMasVendidos(int limit) {
       return repository.findMasVendidos(limit);
    }
}
