package com.uamishop.ordenes.ordenes.repository;

import com.uamishop.ordenes.ordenes.domain.Orden;
import com.uamishop.ordenes.ordenes.domain.OrdenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenJpaRepository extends JpaRepository<Orden, OrdenId> {
}