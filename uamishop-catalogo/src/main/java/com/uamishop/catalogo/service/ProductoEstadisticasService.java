package com.uamishop.catalogo.service;

import com.uamishop.catalogo.domain.ProductoEstadisticas;
import com.uamishop.catalogo.repository.ProductoEstadisticasJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductoEstadisticasService {

    private final ProductoEstadisticasJpaRepository estadisticasRepository;

    public ProductoEstadisticasService(ProductoEstadisticasJpaRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
    }

    @Transactional
    public void registrarVenta(UUID productoId, int cantidad) {
        ProductoEstadisticas stats = estadisticasRepository.findById(productoId)
                .orElse(new ProductoEstadisticas(productoId));
        stats.registrarVenta(cantidad, Instant.now());
        estadisticasRepository.save(stats);
    }

    @Transactional
    public void registrarAgregadoAlCarrito(UUID productoId) {
        ProductoEstadisticas stats = estadisticasRepository.findById(productoId)
                .orElse(new ProductoEstadisticas(productoId));
        stats.registrarAgregadoAlCarrito(Instant.now());
        estadisticasRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public List<ProductoEstadisticas> obtenerMasVendidos(int limit) {
        return estadisticasRepository.findMasVendidos(limit);
    }

    @Transactional(readOnly = true)
    public ProductoEstadisticas obtenerEstadisticas(UUID productoId) {
        return estadisticasRepository.findById(productoId).orElse(null);
    }
}