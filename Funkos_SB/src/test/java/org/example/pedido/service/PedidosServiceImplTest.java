package org.example.pedido.service;

import org.bson.types.ObjectId;
import org.example.categoria.models.Categoria;
import org.example.funko.models.Descripcion;
import org.example.funko.models.Funko;
import org.example.funko.repository.FunkoRepository;
import org.example.pedido.exceptions.*;
import org.example.pedido.models.LineaPedido;
import org.example.pedido.models.Pedido;
import org.example.pedido.repository.PedidosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidosServiceImplTest {
    private final Descripcion descripcion = new Descripcion("SoyTest");
    private final Categoria categoria = new Categoria(null, "TEST", null, LocalDateTime.now(), LocalDateTime.now(), true);
    private final Funko funko = new Funko(null, "Funko Test", descripcion, categoria,"soy.png", 19.99 , 5,  LocalDateTime.now(), LocalDateTime.now());
    @Mock
    private PedidosRepository pedidosRepository;
    @Mock
    private FunkoRepository funkoRepository;

    @InjectMocks
    private PedidosServiceImpl pedidosService;


    @Test
    void findAll_ReturnsPageOfPedidos() {
        // Arrange
        List<Pedido> pedidos = List.of(new Pedido(), new Pedido());
        Page<Pedido> expectedPage = new PageImpl<>(pedidos);
        Pageable pageable = PageRequest.of(0, 10);

        when(pedidosRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Pedido> result = pedidosService.findAll(pageable);

        // Assert
        assertAll(
                () -> assertEquals(expectedPage, result),
                () -> assertEquals(expectedPage.getContent(), result.getContent()),
                () -> assertEquals(expectedPage.getTotalElements(), result.getTotalElements())
        );

        // Verify
        verify(pedidosRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindById() {
        // Arrange
        ObjectId idPedido = new ObjectId();
        Pedido expectedPedido = new Pedido();
        when(pedidosRepository.findById(idPedido)).thenReturn(Optional.of(expectedPedido));

        // Act
        Pedido resultPedido = pedidosService.findById(idPedido);

        // Assert
        assertEquals(expectedPedido, resultPedido);

        // Verify
        verify(pedidosRepository).findById(idPedido);
    }

    @Test
    void testFindById_ThrowsPedidoNotFound() {
        // Arrange
        ObjectId idPedido = new ObjectId();
        when(pedidosRepository.findById(idPedido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PedidoNotFound.class, () -> pedidosService.findById(idPedido));

        // Verify
        verify(pedidosRepository).findById(idPedido);
    }

    @Test
    void testFindByIdUsuario() {
        // Arrange
        Long idUsuario = 1L;
        Pageable pageable = mock(Pageable.class);
        @SuppressWarnings("unchecked")
        Page<Pedido> expectedPage = mock(Page.class);
        when(pedidosRepository.findByIdUsuario(idUsuario, pageable)).thenReturn(expectedPage);

        // Act
        Page<Pedido> resultPage = pedidosService.findByIdUsuario(idUsuario, pageable);

        // Assert
        assertEquals(expectedPage, resultPage);

        // Verify
        verify(pedidosRepository).findByIdUsuario(idUsuario, pageable);
    }

    @Test
    void testSave() {

        Pedido pedido = new Pedido();
        LineaPedido lineaPedido = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(19.99)
                .build();
        pedido.setLineasPedido(List.of(lineaPedido));
        Pedido pedidoToSave = new Pedido();
        pedidoToSave.setLineasPedido(List.of(lineaPedido));

        when(pedidosRepository.save(any(Pedido.class))).thenReturn(pedidoToSave);
        when(funkoRepository.findById(anyLong())).thenReturn(Optional.of(funko));

        // Act
        Pedido resultPedido = pedidosService.save(pedido);

        // Assert
        assertAll(
                () -> assertEquals(pedidoToSave, resultPedido),
                () -> assertEquals(pedidoToSave.getLineasPedido(), resultPedido.getLineasPedido()),
                () -> assertEquals(pedidoToSave.getLineasPedido().size(), resultPedido.getLineasPedido().size())
        );

        // Verify
        verify(pedidosRepository).save(any(Pedido.class));
        verify(funkoRepository, times(2)).findById(anyLong());
    }

    @Test
    void testSave_ThrowsPedidoNotItems() {
        // Arrange
        Pedido pedido = new Pedido();

        // Act & Assert
        assertThrows(PedidoNotItems.class, () -> pedidosService.save(pedido));

        // Verify
        verify(pedidosRepository, never()).save(any(Pedido.class));
        verify(funkoRepository, never()).findById(anyLong());
    }

    @Test
    void testDelete() {
        // Arrange
        ObjectId idPedido = new ObjectId();
        Pedido pedidoToDelete = new Pedido();
        when(pedidosRepository.findById(idPedido)).thenReturn(Optional.of(pedidoToDelete));

        // Act
        pedidosService.delete(idPedido);

        // Assert


        // Verify
        verify(pedidosRepository).findById(idPedido);
        verify(pedidosRepository).deleteById(idPedido);
    }

    @Test
    void testDelete_ThrowsPedidoNotFound() {
        // Arrange
        ObjectId idPedido = new ObjectId();
        when(pedidosRepository.findById(idPedido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PedidoNotFound.class, () -> pedidosService.delete(idPedido));

        // Verify
        verify(pedidosRepository).findById(idPedido);
        verify(pedidosRepository, never()).deleteById(idPedido);
    }

    @Test
    void testUpdate() {

        LineaPedido lineaPedido = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(19.99)
                .build();

        ObjectId idPedido = new ObjectId();
        Pedido pedido = new Pedido();
        pedido.setLineasPedido(List.of(lineaPedido));
        Pedido pedidoToUpdate = new Pedido();
        pedidoToUpdate.setLineasPedido(List.of(lineaPedido)); // Inicializar la lista de líneas de pedido

        when(pedidosRepository.findById(idPedido)).thenReturn(Optional.of(pedidoToUpdate));
        when(pedidosRepository.save(any(Pedido.class))).thenReturn(pedidoToUpdate);
        when(funkoRepository.findById(anyLong())).thenReturn(Optional.of(funko));

        // Act
        Pedido resultPedido = pedidosService.update(idPedido, pedido);

        // Assert
        assertAll(
                () -> assertEquals(pedidoToUpdate, resultPedido),
                () -> assertEquals(pedidoToUpdate.getLineasPedido(), resultPedido.getLineasPedido()),
                () -> assertEquals(pedidoToUpdate.getLineasPedido().size(), resultPedido.getLineasPedido().size())
        );

        // Verify
        verify(pedidosRepository).findById(idPedido);
        verify(pedidosRepository).save(any(Pedido.class));
        verify(funkoRepository, times(3)).findById(anyLong());
    }

    @Test
    void testUpdate_ThrowsPedidoNotFound() {
        // Arrange
        ObjectId idPedido = new ObjectId();
        Pedido pedido = new Pedido();
        when(pedidosRepository.findById(idPedido)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PedidoNotFound.class, () -> pedidosService.update(idPedido, pedido));

        // Verify
        verify(pedidosRepository).findById(idPedido);
        verify(pedidosRepository, never()).save(any(Pedido.class));
        verify(funkoRepository, never()).findById(anyLong());
    }

    @Test
    void testReserveStockPedidos() throws PedidoNotFound, ProductoNotFound, ProductoBadPrice {
        // Arrange
        Pedido pedido = new Pedido();
        List<LineaPedido> lineasPedido = new ArrayList<>();
        LineaPedido lineaPedido1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(10.0)
                .build();

        lineasPedido.add(lineaPedido1); // Agregar la línea de pedido a la lista

        pedido.setLineasPedido(lineasPedido); // Asignar la lista de líneas de pedido al pedido


        when(funkoRepository.findById(1L)).thenReturn(Optional.of(funko));

        // Act
        Pedido result = pedidosService.reserveStockPedidos(pedido);

        // Assert
        assertAll(
                () -> assertEquals(3, funko.getStock()), // Verifica que el stock se haya actualizado correctamente
                () -> assertEquals(20.0, lineaPedido1.getTotal()), // Verifica que el total de la línea de pedido se haya calculado correctamente
                () -> assertEquals(20.0, result.getTotal()), // Verifica que el total del pedido se haya calculado correctamente
                () -> assertEquals(2, result.getTotalItems()) // Verifica que el total de items del pedido se haya calculado correctamente
        );

        // Verify
        verify(funkoRepository, times(1)).findById(1L);
        verify(funkoRepository, times(1)).save(funko);
    }

    @Test
    void returnStockPedidos_ShouldReturnPedidoWithUpdatedStock() {
        // Arrange
        Pedido pedido = new Pedido();
        List<LineaPedido> lineasPedido = new ArrayList<>();
        LineaPedido lineaPedido1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .build();

        lineasPedido.add(lineaPedido1);
        pedido.setLineasPedido(lineasPedido);


        when(funkoRepository.findById(1L)).thenReturn(Optional.of(funko));
        when(funkoRepository.save(funko)).thenReturn(funko);

        // Act
        Pedido result = pedidosService.returnStockPedidos(pedido);

        // Assert
        assertEquals(7, funko.getStock());
        assertEquals(pedido, result);

        // Verify
        verify(funkoRepository, times(1)).findById(1L);
        verify(funkoRepository, times(1)).save(funko);
    }

    @Test
    void checkPedido_ProductosExistenYHayStock_NoDebeLanzarExcepciones() {
        // Arrange
        Pedido pedido = new Pedido();
        List<LineaPedido> lineasPedido = new ArrayList<>();
        LineaPedido lineaPedido1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(19.99)
                .build();

        lineasPedido.add(lineaPedido1);
        pedido.setLineasPedido(lineasPedido);

        when(funkoRepository.findById(1L)).thenReturn(Optional.of(funko));


        // Act & Assert
        assertDoesNotThrow(() -> pedidosService.checkPedido(pedido));

        // Verify
        verify(funkoRepository, times(1)).findById(1L);
    }

    @Test
    void checkPedido_ProductoNoExiste_DebeLanzarProductoNotFound() {
        // Arrange
        Pedido pedido = new Pedido();
        List<LineaPedido> lineasPedido = new ArrayList<>();
        LineaPedido lineaPedido1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(10.0)
                .build();

        lineasPedido.add(lineaPedido1);
        pedido.setLineasPedido(lineasPedido);

        when(funkoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoNotFound.class, () -> pedidosService.checkPedido(pedido));

        // Verify
        verify(funkoRepository, times(1)).findById(1L);
    }

    @Test
    void checkPedido_ProductoNoTieneSuficienteStock_DebeLanzarProductoNotStock() {
        // Arrange
        Pedido pedido = new Pedido();
        List<LineaPedido> lineasPedido = new ArrayList<>();
        LineaPedido lineaPedido1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(10.0)
                .build();
        lineaPedido1.setIdProducto(1L);
        lineaPedido1.setCantidad(10);
        lineasPedido.add(lineaPedido1);
        pedido.setLineasPedido(lineasPedido);


        when(funkoRepository.findById(1L)).thenReturn(Optional.of(funko));

        // Act & Assert
        assertThrows(ProductoNotStock.class, () -> pedidosService.checkPedido(pedido));

        // Verify
        verify(funkoRepository, times(1)).findById(1L);
    }

    @Test
    void checkPedido_PrecioProductoDiferente_DebeLanzarProductoBadPrice() {
        // Arrange
        Pedido pedido = new Pedido();
        List<LineaPedido> lineasPedido = new ArrayList<>();
        LineaPedido lineaPedido1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(10.0)
                .build();
        lineaPedido1.setIdProducto(1L);
        lineaPedido1.setCantidad(2);
        lineaPedido1.setPrecioProducto(20.0); // Precio diferente al del producto
        lineasPedido.add(lineaPedido1);
        pedido.setLineasPedido(lineasPedido);


        when(funkoRepository.findById(1L)).thenReturn(Optional.of(funko));

        // Act & Assert
        assertThrows(ProductoBadPrice.class, () -> pedidosService.checkPedido(pedido));

        // Verify
        verify(funkoRepository, times(1)).findById(1L);
    }

}