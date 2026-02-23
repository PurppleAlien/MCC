package com.example.demo.ventas.repository;

import com.example.demo.ventas.domain.Carrito;
import com.example.demo.ventas.domain.CarritoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, CarritoId> {
    Optional<Carrito> findById(CarritoId id);
}