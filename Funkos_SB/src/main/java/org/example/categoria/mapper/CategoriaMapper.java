package org.example.categoria.mapper;


import org.example.categoria.dto.CategoriaDto;
import org.example.categoria.models.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {
    public Categoria fromCategoriaDto(CategoriaDto categoriaDto) {
        var categoria = new Categoria();
        categoria.setTipo(categoriaDto.tipo());
        categoria.setEnabled(categoriaDto.enabled());
        return categoria;
    }

}
