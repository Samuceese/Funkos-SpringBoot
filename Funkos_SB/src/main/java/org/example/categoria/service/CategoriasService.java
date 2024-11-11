package org.example.categoria.service;


import org.example.categoria.models.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CategoriasService {
    Page<Categoria> getCategorias(Optional<String> nombre, Optional<Boolean> isDeleted, Pageable pageable);
    Categoria getById(Long id);
    Categoria getByTipo(String string);
    Categoria create(Categoria categoria);
    Categoria update(Long id, Categoria categoria);
    Categoria delete(Long id);

}
