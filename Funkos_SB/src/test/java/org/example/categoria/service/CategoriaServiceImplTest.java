package org.example.categoria.service;

import org.example.categoria.exceptions.CategoriaException;
import org.example.categoria.models.Categoria;
import org.example.categoria.repository.CategoriasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceImplTest {
    private final Categoria categoria = new Categoria(1L, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);
    @Mock
    private CategoriasRepository categoriaRepository;
    @InjectMocks
    private CategoriasServiceImpl categoriaService;

    @Test
    public void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Categoria> expectedPage = new PageImpl<>(List.of(categoria));
        when(categoriaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);



        var res = categoriaService.getCategorias(Optional.empty(), Optional.empty(), pageable);


        assertAll("findAll",
                () -> assertNotNull(res),
                () -> assertFalse(res.isEmpty())
        );


        verify(categoriaRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getCategoriaById(){
        when(categoriaRepository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        var result = categoriaService.getById(1L);
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(categoria.getId(), result.getId()),
                () -> assertEquals(categoria.getTipo(), result.getTipo())
        );
        verify(categoriaRepository, times(1) ).findById(1L);
    }

    @Test
    void getCategoriaByIdNotFound(){
        var id = 99999L;
        when(categoriaRepository.findById(id)).thenThrow(new CategoriaException.CategoriaNotFound(id));
        var result = assertThrows(CategoriaException.CategoriaNotFound.class, () -> categoriaService.getById(id));
        verify(categoriaRepository, times(1) ).findById(id);
    }

    @Test
    void getByTipo(){
        when(categoriaRepository.findByTipo(categoria.getTipo())).thenReturn(categoria);
        var result = categoriaService.getByTipo(categoria.getTipo());
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(categoria.getId(), result.getId()),
                () -> assertEquals(categoria.getTipo(), result.getTipo())
        );
        verify(categoriaRepository, times(1) ).findByTipo(categoria.getTipo());
    }


    @Test
    void save(){
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        var result = categoriaService.create(new Categoria(null, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true));
        assertAll("save",
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getId())
        );
        verify(categoriaRepository, times(1) ).save(any(Categoria.class));
    }

    @Test
    void update(){
        when(categoriaRepository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        var result = categoriaService.update(1L, new Categoria(1L, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true));
        assertAll("update",
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("TEST", result.getTipo())
        );
        verify(categoriaRepository, times(1) ).findById(1L);
        verify(categoriaRepository, times(1) ).save(any(Categoria.class));
    }

    @Test
    void updateNotFound(){
        var id = 9999L;
        when(categoriaRepository.findById(id)).thenThrow(new CategoriaException.CategoriaNotFound(id));
        var result = assertThrows(CategoriaException.CategoriaNotFound.class, () -> categoriaService.getById(id));
        verify(categoriaRepository, times(1) ).findById(id);
        verify(categoriaRepository, times(0)).save(categoria);
    }

    @Test
    void delete(){
        when(categoriaRepository.findById(1L)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(new Categoria(1L, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), false));
        var result = categoriaService.delete(1L);
        assertAll("delete",
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getId()),
                () -> assertFalse(result.getEnabled())
        );
        verify(categoriaRepository, times(1) ).findById(1L);
        verify(categoriaRepository, times(1) ).save(any(Categoria.class));
    }

    @Test
    void deleteNotFound(){
        var id = 9999L;
        when(categoriaRepository.findById(id)).thenThrow(new CategoriaException.CategoriaNotFound(id));
        var result = assertThrows(CategoriaException.CategoriaNotFound.class, () -> categoriaService.delete(id));
        verify(categoriaRepository, times(1) ).findById(id);
        verify(categoriaRepository, times(0)).save(categoria);
    }




}
