package com.uamishop.catalogo.repository;

import com.uamishop.catalogo.domain.Producto;
import com.uamishop.catalogo.shared.domain.ProductoId; // <-- Importación corregida
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoJpaRepository extends JpaRepository<Producto, ProductoId> {
}