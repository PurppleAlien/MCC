package com.example.demo.ordenes.repository;

import com.example.demo.ordenes.domain.Orden;
import com.example.demo.ordenes.domain.OrdenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenJpaRepository extends JpaRepository<Orden, OrdenId> {
}