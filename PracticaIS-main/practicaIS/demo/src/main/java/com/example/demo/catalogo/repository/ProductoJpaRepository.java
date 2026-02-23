package com.example.demo.catalogo.repository;

import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.domain.ProductoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoJpaRepository extends JpaRepository<Producto, ProductoId> {
}