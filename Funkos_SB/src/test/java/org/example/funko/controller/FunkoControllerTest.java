package org.example.funko.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.categoria.models.Categoria;
import org.example.funko.dto.FunkoDto;
import org.example.funko.exceptions.FunkosExceptions;
import org.example.funko.mapper.FunkosMapper;
import org.example.funko.models.Descripcion;
import org.example.funko.models.Funko;
import org.example.funko.service.FunkosService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ExtendWith(MockitoExtension.class)
public class FunkoControllerTest {
    private final String myEndpoint = "/funkos/v1/funkos";
    private final Descripcion descripcion = new Descripcion("SoyTest");
    private final Categoria categoria = new Categoria(1L, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);
    private final Funko funko = new Funko(1L, "FunkoTest", descripcion, categoria,"soy.png", 19.99 , 1, LocalDateTime.now(), LocalDateTime.now());


    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private FunkosService funkosService;
    @MockBean
    private FunkosMapper mapperFunko;
    @Autowired
    private JacksonTester<FunkoDto> funkoDto;
    @Autowired
    public FunkoControllerTest(FunkosService funkosService, FunkosMapper funkosMapperFunko){
        this.funkosService = funkosService;
        this.mapperFunko = funkosMapperFunko;
        mapper.registerModule(new JavaTimeModule());

    }

    @Test
    void getAllFunkos() throws Exception {
        var productosList = List.of(funko);
        var pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        var page = new PageImpl<>(productosList);

        when(funkosService.getAllFunkos(Optional.empty(), Optional.empty(), Optional.empty(), pageable)).thenReturn(page);

        MockHttpServletResponse response = mockMvc.perform(
                        get(myEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<Funko> res = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        assertAll(
                () -> assertEquals(200, response.getStatus())
        );

        verify(funkosService, times(1)).getAllFunkos(Optional.empty(),  Optional.empty(), Optional.empty(), pageable);
    }

    @Test
    void getFunkoById() throws Exception {
        var myLocalEndpoint = "/funkos/v1/funkos/1";  // Verifica la URL

        // Arrange
        when(funkosService.getFunkoById(anyLong())).thenReturn(funko);  // Asegúrate de que `funko` no sea null

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Funko res = mapper.readValue(response.getContentAsString(), Funko.class);

        // Assert
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(funko.getId(), res.getId()),
                () -> assertEquals(funko.getName(), res.getName()),
                () -> assertEquals(funko.getDescripcion().getDescripcion(), res.getDescripcion().getDescripcion())
        );
        // Verify
        verify(funkosService, times(1)).getFunkoById(anyLong());
    }


    @Test
    void getProductByIdNotFound() throws Exception {
        var myLocalEndpoint = myEndpoint + "/1";

        // Arrange
        when(funkosService.getFunkoById(anyLong())).thenThrow(new FunkosExceptions.FunkoNotFound(funko.getId()));

        // Consulto el endpoint
        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();


        // Assert
        assertEquals(404, response.getStatus());

        // Verify
        verify(funkosService, times(1)).getFunkoById(anyLong());
    }

    @Test
    void getFunkoByName() throws Exception {
        var myLocalEndpoint = "/funkos/v1/funkos/nombre/FunkoTest";  // Asegúrate de que la URL es correcta;
        when(funkosService.getFunkoByName(funko.getName())).thenReturn(funko);  // Mock del servicio

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Funko res = mapper.readValue(response.getContentAsString(), Funko.class);

        // Assert
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(funko.getName(), res.getName())
        );
        // Verify
        verify(funkosService, times(1)).getFunkoByName(funko.getName());
    }


    @Test
    void getFunkoByNameNotFound() throws Exception {
        var myLocalEndpoint = myEndpoint + "/nombre/test";

        // Arrange
        when(funkosService.getFunkoByName("test")).thenThrow(new FunkosExceptions.FunkoNotFoundByName(funko.getName()));

        // Consulto el endpoint
        MockHttpServletResponse response = mockMvc.perform(
                        get(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();


        // Assert
        assertEquals(404, response.getStatus());

        // Verify
        verify(funkosService, times(1)).getFunkoByName("test");
    }

    @Test
    void createFunko() throws Exception {
        var funkoDto = new FunkoDto(
                "Funko test",
                "TEST",
                "SoyTest",
                "soy.png",
                19.99
        );

        when(mapperFunko.fromFunkoDto(funkoDto)).thenReturn(funko);
        when(funkosService.createFunko(funko)).thenReturn(funko);

        MockHttpServletResponse response = mockMvc.perform(
                        post("/funkos/v1/funkos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(funkoDto))
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertAll(
                () -> assertEquals(201, response.getStatus())
        );

        verify(funkosService, times(1)).createFunko(funko);
    }



    @Test
    void createFunkoBadRequest() throws Exception {
        var funkoNew = new FunkoDto("Funko test", "TEST", "SoyTest", "soy.png", 199.99);
        when(funkosService.createFunko(any(Funko.class))).thenReturn(funko);
        MockHttpServletResponse response = mockMvc.perform(
                        post(myEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                // Le paso el body
                                .content(funkoDto.write(funkoNew).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertAll(
                () -> assertEquals(400, response.getStatus())
        );
    }

    @Test
    void updateFunko() throws Exception {
        var myLocalEndpoint = myEndpoint + "/1";
        var funkoUpdated = new FunkoDto("Funko test updated", "TEST", "SoyTestUpdated", "soy.png", 29.99);
        var Newcategoria = new Categoria("TEST");
        var Newdescripcion = new Descripcion("SoyTestUpdated");
        var newFunko = new Funko(1L, "Funko test updated", Newdescripcion, Newcategoria, "soy.png", 29.99, 1, LocalDateTime.now(), LocalDateTime.now() );
        when(mapperFunko.fromFunkoDto(funkoUpdated)).thenReturn(newFunko);
        when(funkosService.updateFunko(anyLong(), any(Funko.class))).thenReturn(newFunko);
        MockHttpServletResponse response = mockMvc.perform(
                        put(myLocalEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(funkoDto.write(funkoUpdated).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Funko res = mapper.readValue(response.getContentAsString(), Funko.class);
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(newFunko.getName(), res.getName()),
                () -> assertEquals(newFunko.getDescripcion().getDescripcion(), res.getDescripcion().getDescripcion())
        );
        verify(funkosService, times(1)).updateFunko(anyLong(), any(Funko.class));
    }

    @Test
    void updateFunkoNotFound() throws Exception {
        var myLocalEndpoint = myEndpoint + "/99999";
        var funkoUpdated = new FunkoDto("Funko test updated", "TEST", "SoyTestUpdated", "soy.png", 29.99);
        var Newcategoria = new Categoria("TEST");
        var Newdescripcion = new Descripcion("SoyTestUpdated");
        var newFunko = new Funko(null, "Funko test updated", Newdescripcion, Newcategoria, "soy.png", 29.99, 1 , LocalDateTime.now(), LocalDateTime.now() );
        when(mapperFunko.fromFunkoDto(funkoUpdated)).thenReturn(newFunko);
        when(funkosService.updateFunko(anyLong(), any(Funko.class)))
                .thenThrow(new FunkosExceptions.FunkoNotFound(anyLong()));


        MockHttpServletResponse response = mockMvc.perform(
                        put(myLocalEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(funkoDto.write(funkoUpdated).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(404, response.getStatus());
    }

    @Test
    void updateFunkoBadRequest() throws Exception{
        var myLocalEndpoint = myEndpoint + "/1";
        var funkoUpdated = new FunkoDto("Funko test updated", "TEST", "SoyTestUpdated", "soy.png", 229.99);

        MockHttpServletResponse response = mockMvc.perform(
                        put(myLocalEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(funkoDto.write(funkoUpdated).getJson())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(400, response.getStatus());
    }

    @Test
    void deleteFunko() throws Exception {
        var myLocalEndpoint = myEndpoint + "/1";
        when(funkosService.deleteFunko(anyLong())).thenReturn(funko);
        MockHttpServletResponse response = mockMvc.perform(
                        delete(myLocalEndpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertAll(
                () -> assertEquals(204, response.getStatus())
        );
        verify(funkosService, times(1)).deleteFunko(anyLong());
    }

    @Test
    void deleteFunkoNotFound() throws Exception {
        var myLocalEndpoint = myEndpoint + "/99999";
        when(funkosService.deleteFunko(anyLong())).thenThrow(new FunkosExceptions.FunkoNotFound(anyLong()));
        MockHttpServletResponse response = mockMvc.perform(
                        delete(myLocalEndpoint)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(404, response.getStatus());
    }



}
