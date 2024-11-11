package org.example.funko.repository;


import org.example.categoria.models.Categoria;
import org.example.funko.models.Descripcion;
import org.example.funko.models.Funko;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FunkoRepositoryTest {
    private final Descripcion descripcion = new Descripcion("SoyTest");
    private final Categoria categoria = new Categoria(null, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);
    private final Funko funko = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 19.99 ,1, LocalDateTime.now(), LocalDateTime.now());

    @Autowired
    private FunkoRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp(){
        entityManager.persist(categoria);
        entityManager.flush();

        funko.setCategoria(categoria);
        entityManager.persist(funko);
        entityManager.flush();
    }


    @Test
    void findByName() {
        String name = "Funko Test";
        Funko result = repository.findByName(name);
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(funko.getName(), result.getName())
        );
    }
}
