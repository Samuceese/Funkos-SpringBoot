package org.example.categoria.mapper;

import org.example.categoria.dto.CategoriaDto;
import org.example.categoria.models.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class CategoriaMapperTest {

    private CategoriaMapper mapper;

    @BeforeEach
    void setUp(){
        mapper = new CategoriaMapper();
    }

    @Test
    void fromCategoriaDto_ShouldMapCorrectly() {
        CategoriaDto dto = new CategoriaDto("TEST", true);

        Categoria categoria = mapper.fromCategoriaDto(dto);

        assertNotNull(categoria);
        assertEquals(dto.tipo(), categoria.getTipo());
        assertEquals(dto.enabled(), categoria.getEnabled());
    }
}