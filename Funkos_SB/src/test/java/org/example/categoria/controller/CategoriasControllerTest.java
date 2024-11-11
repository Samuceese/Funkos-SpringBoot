package org.example.categoria.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.categoria.dto.CategoriaDto;
import org.example.categoria.exceptions.CategoriaException;
import org.example.categoria.mapper.CategoriaMapper;
import org.example.categoria.models.Categoria;
import org.example.categoria.service.CategoriasService;
import org.example.funko.exceptions.FunkosExceptions;
import org.example.utils.pageresponse.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ExtendWith(MockitoExtension.class)
public class CategoriasControllerTest {
    private final String myEndpoint = "/funkos/v1/categorias";
    private final Categoria categoria = new Categoria(1L, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private CategoriasService categoriasService;
    @MockBean
    private CategoriaMapper categoriaMapper;
    @Autowired
    private JacksonTester<CategoriaDto> categoriaDto;

    @Autowired
    public CategoriasControllerTest(CategoriasService categoriasService, CategoriaMapper categoriaMapper) {
        this.categoriasService = categoriasService;
        this.categoriaMapper = categoriaMapper;
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getAllCategorias() throws Exception {
        var list = List.of(categoria);
        Page<Categoria> page = new PageImpl<>(list);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        when(categoriasService.getCategorias(Optional.empty(),Optional.empty(),pageable)).thenReturn(page);

        MockHttpServletResponse response = mockMvc.perform(
                        get(myEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<Categoria> res = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        assertEquals(200, response.getStatus());

        verify(categoriasService, times(1)).getCategorias(Optional.empty(), Optional.empty(), pageable);
    }

    @Test
    void getCategoriaById() throws Exception {
        var myLocalEndpoint = "/funkos/v1/categorias/1";

        when(categoriasService.getById(1L)).thenReturn(categoria);

        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Categoria res = mapper.readValue(response.getContentAsString(), Categoria.class);

        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(categoria.getId(), res.getId()),
                () -> assertEquals(categoria.getTipo(), res.getTipo())
        );


        verify(categoriasService, times(1)).getById(1L);
    }

    @Test
    void getCategoriaByIdNotFound() throws Exception {
        var myLocalEndpoint = "/funkos/v1/categorias/99999";

        when(categoriasService.getById(99999L)).thenThrow(new CategoriaException.CategoriaNotFound(99999L));

        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();


        assertEquals(404, response.getStatus());

        verify(categoriasService, times(1)).getById(99999L);
    }

    @Test
    void getCategoriaByTipo() throws Exception {
        var myLocalEndpoint = "/funkos/v1/categorias/tipo/TEST";
        when(categoriasService.getByTipo("TEST")).thenReturn(categoria);

        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Categoria res = mapper.readValue(response.getContentAsString(), Categoria.class);


        assertEquals(200, response.getStatus());
        assertEquals(res.getTipo(), categoria.getTipo());

        verify(categoriasService, times(1)).getByTipo("TEST");
    }

    @Test
    void getCategoriaByTipoNotFound() throws Exception {
        var myLocalEndpoint = "/funkos/v1/categorias/tipo/NO";

        when(categoriasService.getByTipo("NO")).thenThrow(new CategoriaException.CategoriaNotFoundByTipo("NO"));

        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(404, response.getStatus());

        verify(categoriasService, times(1)).getByTipo("NO");
    }

    @Test
    void createCategoriaOk() throws Exception {
        var categoriaDto = new CategoriaDto(
                "TEST",
                false
        );

        when(categoriaMapper.fromCategoriaDto(categoriaDto)).thenReturn(categoria);
        when(categoriasService.create(categoria)).thenReturn(categoria);

        MockHttpServletResponse response = mockMvc.perform(
                        post("/funkos/v1/categorias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(categoriaDto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(201, response.getStatus());

        verify(categoriasService, times(1)).create(categoria);
    }

    @Test
    void createFunkoBadRequest() throws Exception {
        var categoriaNew = new CategoriaDto(
                "",
                null
        );

        when(categoriasService.create(any(Categoria.class))).thenReturn(categoria);
        MockHttpServletResponse response = mockMvc.perform(
                        post("/funkos/v1/categorias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoriaDto.write(categoriaNew).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(400, response.getStatus());
    }

    @Test
    void updateFunkoOk() throws Exception {
        var myLocalEndPoint = myEndpoint + "/1";
        var categoriaUpdated = new CategoriaDto(
                "TEST_UPDATE",
                false
        );
        var newCategoria = new Categoria("TEST");

        when(categoriaMapper.fromCategoriaDto(categoriaUpdated)).thenReturn(newCategoria);
        when(categoriasService.update(anyLong(), any(Categoria.class))).thenReturn(newCategoria);

        MockHttpServletResponse response = mockMvc.perform(
                        put(myLocalEndPoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoriaDto.write(categoriaUpdated).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Categoria res = mapper.readValue(response.getContentAsString(), Categoria.class);

        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(newCategoria.getTipo(), res.getTipo())
        );
        verify(categoriasService, times(1)).update(anyLong(), any(Categoria.class));
    }

    @Test
    void updateCategoriaNotFound() throws Exception {
        var myLocalEndPoint = myEndpoint + "/1";
        var categoriaUpdated = new CategoriaDto(
                "TEST_UPDATE",
                false
        );
        var newCategoria = new Categoria("TEST");

        when(categoriaMapper.fromCategoriaDto(categoriaUpdated)).thenReturn(newCategoria);
        when(categoriasService.update(anyLong(), any(Categoria.class)))
                .thenThrow(new FunkosExceptions.FunkoNotFound(anyLong()));


        MockHttpServletResponse response = mockMvc.perform(
                        put(myLocalEndPoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoriaDto.write(categoriaUpdated).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(404, response.getStatus());
    }

    @Test
    void updateCategoriaBadRequest() throws Exception{
        var myLocalEndpoint = myEndpoint + "/1";
        var categoriaUpdated = new CategoriaDto(
                "",
                null
        );
        MockHttpServletResponse response = mockMvc.perform(
                        post(myLocalEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoriaDto.write(categoriaUpdated).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(405, response.getStatus());
    }

    @Test
    void deleteCategoria() throws Exception {
        var myLocalEndpoint = myEndpoint + "/1";
        when(categoriasService.delete(anyLong())).thenReturn(categoria);
        MockHttpServletResponse response = mockMvc.perform(
                        delete(myLocalEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertAll(
                () -> assertEquals(204, response.getStatus())
        );
        verify(categoriasService, times(1)).delete(anyLong());
    }

    @Test
    void deleteCategoriaNotFound() throws Exception {
        var myLocalEndpoint = myEndpoint + "/99999";
        when(categoriasService.delete(anyLong())).thenThrow(new CategoriaException.CategoriaNotFound(99999L));
        MockHttpServletResponse response = mockMvc.perform(
                        delete(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(404, response.getStatus());
    }
}




