package org.example.categoria.repository;

import org.example.categoria.models.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CategoriaRepositoryTest {
    private final Categoria categoria = new Categoria(1L, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);
    @Autowired
    private CategoriasRepository repository;
    @Autowired
    private TestEntityManager entityManager;
    @BeforeEach
    void setUp() {
        entityManager.merge(categoria);
    }

    @Test
    void findByTipo() {
        Categoria categoriaEncontrada = repository.findByTipo("TEST");
        assertNotNull(categoriaEncontrada);
        assertEquals(categoria.getTipo(), categoriaEncontrada.getTipo());
        assertEquals(categoria.getCreatedAt(), categoriaEncontrada.getCreatedAt());
    }

    @Test
    void findByTipoNoEncontrada() {
        Categoria categoriaEncontrada = repository.findByTipo("TEST2");
        assertEquals(null, categoriaEncontrada);
    }
}
