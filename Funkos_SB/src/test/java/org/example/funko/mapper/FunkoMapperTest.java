package org.example.funko.mapper;

import org.example.funko.dto.FunkoDto;
import org.example.funko.models.Funko;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FunkoMapperTest {
    private FunkosMapper mapper;
    @BeforeEach
    void setUp() {
        mapper = new FunkosMapper();
    }
    @Test
    void fromFunkoDtoOk() {
        FunkoDto dto = new FunkoDto("Funko test", "TEST", "SoyTest", "soy.png", 19.99);
        Funko funko = mapper.fromFunkoDto(dto);

        assertNotNull(funko);
        assertEquals(dto.name(), funko.getName());
        assertEquals(dto.price(), funko.getPrice());


    }

}
