package com.uamishop.catalogo.repository;

import com.uamishop.catalogo.domain.Categoria;
import com.uamishop.catalogo.domain.CategoriaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaJpaRepository extends JpaRepository<Categoria, CategoriaId> {
}