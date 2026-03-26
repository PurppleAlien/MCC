package com.uamishop.catalogo.repository;

import com.uamishop.catalogo.domain.ProductoEstadisticas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.UUID;

public interface ProductoEstadisticasJpaRepository extends JpaRepository<ProductoEstadisticas, UUID> {

    default List<ProductoEstadisticas> findMasVendidos(int limit) {
        return findAll(Sort.by(Sort.Direction.DESC, "cantidadVendida"))
            .stream()
            .limit(limit)
            .toList();
    }
}