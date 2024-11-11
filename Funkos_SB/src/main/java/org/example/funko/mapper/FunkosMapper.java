package org.example.funko.mapper;


import org.example.categoria.models.Categoria;
import org.example.funko.dto.FunkoDto;
import org.example.funko.models.Descripcion;
import org.example.funko.models.Funko;
import org.springframework.stereotype.Component;

@Component
public class FunkosMapper {

    public Funko fromFunkoDto(FunkoDto dto) {
        var funko = new Funko();
        funko.setName(dto.name());
        funko.setCategoria(new Categoria(dto.categoria()));
        funko.setPrice(dto.price());
        funko.setImagen(dto.imagen());
        Descripcion descripcion = new Descripcion(dto.description());
        funko.setDescripcion(descripcion);
        return funko;
    }

}
