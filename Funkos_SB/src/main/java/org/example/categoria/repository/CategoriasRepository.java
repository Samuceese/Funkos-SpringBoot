package org.example.categoria.repository;

import org.example.categoria.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CategoriasRepository extends JpaRepository<Categoria, Long>, JpaSpecificationExecutor<Categoria> {
    Categoria findByTipo(String string);
}
