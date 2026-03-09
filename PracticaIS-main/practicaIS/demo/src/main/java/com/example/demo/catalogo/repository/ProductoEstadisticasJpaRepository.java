package com.example.demo.catalogo.repository;
import com.example.demo.catalogo.domain.Productoestadisticas; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.data.domain.Sort;


public interface ProductoEstadisticasJpaRepository extends JpaRepository<Productoestadisticas, UUID> {


   default List<Productoestadisticas> findMasVendidos(int limit) {
       return findAll(Sort.by(Sort.Direction.DESC, "cantidadVendida"))
           .stream()
           .limit(limit)
           .toList();
   }
}
    
