package org.example.funko.service;

import jakarta.validation.ConstraintViolationException;
import org.example.categoria.models.Categoria;
import org.example.funko.exceptions.FunkosExceptions;
import org.example.funko.models.Descripcion;
import org.example.funko.models.Funko;
import org.example.funko.repository.FunkoRepository;
import org.example.notification.config.WebSocketConfig;
import org.example.notification.config.WebSocketHandler;
import org.example.notification.mapper.NotificationMapper;
import org.example.notification.models.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FunkoServiceTest {
    private final Descripcion descripcion = new Descripcion("SoyTest");
    private final Categoria categoria = new Categoria(null, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);
    private final Funko funko = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 19.99 , 1,  LocalDateTime.now(), LocalDateTime.now());
    WebSocketHandler webSocketHandlerMock = mock(WebSocketHandler.class);
    @Mock
    private WebSocketConfig webSocketConfig;
    @Mock
    private NotificationMapper mapper;
    @Mock
    private FunkoRepository funkoRepository;
    @InjectMocks
    private FunkosServiceImpl funkosService;





    @Test
    void getAllFunkos() {
        List<Funko> expectedProducts = Arrays.asList(funko);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Funko> expectedPage = new PageImpl<>(expectedProducts);

        when(funkoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);


        Page<Funko> actualPage = funkosService.getAllFunkos(Optional.empty(), Optional.empty(), Optional.empty(), pageable);


        assertAll("findAll",
                () -> assertNotNull(actualPage),
                () -> assertFalse(actualPage.isEmpty()),
                () -> assertEquals(expectedPage, actualPage)
        );

        verify(funkoRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getFunkoById() {
        when(funkoRepository.findById(1L)).thenReturn(java.util.Optional.of(funko));
        Funko result = funkosService.getFunkoById(1L);
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(funko.getId(), result.getId()),
                () -> assertEquals(funko.getName(), result.getName())
        );
        verify(funkoRepository, times(1)).findById(1L);
    }
    @Test
    void getFunkoByIdNotFound() {
        var id = 99999L;
        when(funkoRepository.findById(id)).thenThrow(new FunkosExceptions.FunkoNotFound(id));
        var res = assertThrows(FunkosExceptions.FunkoNotFound.class, () -> funkosService.getFunkoById(id));
        verify(funkoRepository, times(1)).findById(id);
    }

    @Test
    void getFunkoByName() {
        when(funkoRepository.findByName(funko.getName())).thenReturn(funko);
        Funko res = funkoRepository.findByName(funko.getName());
        assertAll(
                () -> assertNotNull(res),
                () -> assertEquals(funko.getId(), res.getId()),
                () -> assertEquals(funko.getName(), res.getName())
        );
        verify(funkoRepository, times(1)).findByName(funko.getName());
    }

    @Test
    void getFunkoByNameNotFound() {
        String nonExistentName = "NonExistentFunko";

        when(funkoRepository.findByName(nonExistentName)).thenReturn(null);

        assertThrows(FunkosExceptions.FunkoNotFoundByName.class, () -> {
            funkosService.getFunkoByName(nonExistentName);
        });

        verify(funkoRepository, times(1)).findByName(nonExistentName);
    }


    @Test
    void createFunko() throws IOException {
        when(funkoRepository.save(funko)).thenReturn(funko);
        doNothing().when(webSocketHandlerMock).sendMessage(any());
        Funko res = funkosService.createFunko(funko);
        assertAll(
                () -> assertNotNull(res),
                () -> assertEquals(funko.getId(), res.getId()),
                () -> assertEquals(funko.getName(), res.getName())
        );
        verify(funkoRepository, times(1)).save(funko);
    }

    @Test
    void createFunkoErrorPrecioPorDebajoLimite(){
        Funko funkoMal = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 1.99 , 1, LocalDateTime.now(), LocalDateTime.now());
        when(funkoRepository.save(funkoMal)).thenThrow( ConstraintViolationException.class);
        var res = assertThrows(ConstraintViolationException.class, () -> funkosService.createFunko(funkoMal));
        verify(funkoRepository, times(1)).save(funkoMal);
    }

    @Test
    void createFunkoErrorPrecioEncimaPrecioLimite(){
        Funko funkoMal = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 100.0 , 1, LocalDateTime.now(), LocalDateTime.now());
        when(funkoRepository.save(funkoMal)).thenThrow(ConstraintViolationException.class);
        var res = assertThrows(ConstraintViolationException.class, () -> funkosService.createFunko(funkoMal));
        verify(funkoRepository, times(1)).save(funkoMal);
    }

    @Test
    void createFunkoLimitePrecioMenor(){
        Funko funkoLimite = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 10.99 , 1, LocalDateTime.now(), LocalDateTime.now());
        when(funkoRepository.save(funkoLimite)).thenReturn(funkoLimite);
        Funko res = funkosService.createFunko(funkoLimite);
        assertAll(
                () -> assertNotNull(res),
                () -> assertEquals(funkoLimite.getId(), res.getId()),
                () -> assertEquals(funkoLimite.getName(), res.getName())
        );
        verify(funkoRepository, times(1)).save(funkoLimite);
    }

    @Test
    void createFunkoLimitePrecioMayor(){
        Funko funkoLimite = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 59.99 , 1, LocalDateTime.now(), LocalDateTime.now());
        when(funkoRepository.save(funkoLimite)).thenReturn(funkoLimite);
        Funko res = funkosService.createFunko(funkoLimite);
        assertAll(
                () -> assertNotNull(res),
                () -> assertEquals(funkoLimite.getId(), res.getId()),
                () -> assertEquals(funkoLimite.getName(), res.getName())
        );
        verify(funkoRepository, times(1)).save(funkoLimite);
    }

    @Test
    void updateFunko() {
        Funko funkoNew = new Funko(null, "Funko Update", descripcion, categoria,"soy.png", 19.99 , 1 , LocalDateTime.now(), LocalDateTime.now());

        when(funkoRepository.findById(funko.getId())).thenReturn(java.util.Optional.of(funko));
        when(funkoRepository.save(any(Funko.class))).thenReturn(funkoNew);

        Funko res = funkosService.updateFunko(funko.getId(), funkoNew);

        assertAll(
                () -> assertNotNull(res),
                () -> assertEquals(funkoNew.getId(), res.getId()),
                () -> assertEquals(funkoNew.getName(), res.getName())
        );
        verify(funkoRepository, times(1)).findById(funko.getId());
        verify(funkoRepository, times(1)).save(any(Funko.class));
    }

    @Test
    void updateFunkoNotFound(){
        Funko funkoNew = new Funko(null, "Funko Update", descripcion, categoria,"soy.png", 19.99 , 1, LocalDateTime.now(), LocalDateTime.now());
        when(funkoRepository.findById(funko.getId())).thenThrow(new FunkosExceptions.FunkoNotFound(funko.getId()));
        var res = assertThrows(FunkosExceptions.FunkoNotFound.class, () -> funkosService.updateFunko(funko.getId(), funko));
        verify(funkoRepository, times(1)).findById(funko.getId());
        verify(funkoRepository, times(0)).save(funkoNew);
    }

    @Test
    void deleteFunko() {
        when(funkoRepository.findById(funko.getId())).thenReturn(java.util.Optional.of(funko));
        doNothing().when(funkoRepository).deleteById(funko.getId());

        var res = funkosService.deleteFunko(funko.getId());

        assertAll(
                () -> assertEquals(funko, res)
        );
        verify(funkoRepository, times(1)).findById(funko.getId());
        verify(funkoRepository, times(1)).deleteById(funko.getId());
    }

    @Test
    void deleteFunkoNotFound(){
        when(funkoRepository.findById(funko.getId())).thenThrow(new FunkosExceptions.FunkoNotFound(funko.getId()));
        var res = assertThrows(FunkosExceptions.FunkoNotFound.class, () -> funkosService.deleteFunko(funko.getId()));
        verify(funkoRepository, times(1)).findById(funko.getId());
        verify(funkoRepository, times(0)).deleteById(funko.getId());
    }


}

