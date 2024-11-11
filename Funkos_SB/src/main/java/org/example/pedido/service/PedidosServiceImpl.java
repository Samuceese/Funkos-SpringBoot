package org.example.pedido.service;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.funko.repository.FunkoRepository;
import org.example.pedido.exceptions.*;
import org.example.pedido.models.LineaPedido;
import org.example.pedido.models.Pedido;
import org.example.pedido.repository.PedidosRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@CacheConfig(cacheNames = {"pedidos"})
public class PedidosServiceImpl implements PedidosService{
    private PedidosRepository pedidosRepository;
    private FunkoRepository funkoRepository;

    public PedidosServiceImpl(PedidosRepository pedidosRepository, FunkoRepository funkoRepository) {
        this.pedidosRepository = pedidosRepository;
        this.funkoRepository = funkoRepository;
    }

    @Override
    public Page<Pedido> findAll(Pageable pageable) {
        log.info("Obteniendo todos los pedidos paginados y ordenados con {}", pageable);
        return pedidosRepository.findAll(pageable);
    }

    @Override
    @Cacheable(key = "#idPedido")
    public Pedido findById(ObjectId idPedido) {
        log.info("Obteniendo pedido con id: " + idPedido);
        return pedidosRepository.findById(idPedido).orElseThrow(() -> new PedidoNotFound(idPedido.toHexString()));
    }

    @Override
    public Page<Pedido> findByIdUsuario(Long idUsuario, Pageable pageable) {
        log.info("Obteniendo pedidos del usuario con id: " + idUsuario);
        return pedidosRepository.findByIdUsuario(idUsuario, pageable);
    }

    @Override
    @Transactional
    @CachePut(key = "#result.id")
    public Pedido save(Pedido pedido) {
        log.info("Guardando pedido: {}", pedido);

        checkPedido(pedido);

        var pedidoToSave = reserveStockPedidos(pedido);

        pedidoToSave.setCreatedAt(LocalDateTime.now());
        pedidoToSave.setUpdatedAt(LocalDateTime.now());

        return pedidosRepository.save(pedidoToSave);
    }

    Pedido reserveStockPedidos(Pedido pedido) {
        log.info("Reservando stock del pedido: {}", pedido);

        if (pedido.getLineasPedido() == null || pedido.getLineasPedido().isEmpty()) {
            throw new PedidoNotItems(pedido.getId().toHexString());
        }

        pedido.getLineasPedido().forEach(lineaPedido -> {
            var producto = funkoRepository.findById(lineaPedido.getIdProducto()).get();
            producto.setStock(producto.getStock() - lineaPedido.getCantidad());
            funkoRepository.save(producto);
            lineaPedido.setTotal(lineaPedido.getCantidad() * lineaPedido.getPrecioProducto());
        });

        var total = pedido.getLineasPedido().stream()
                .map(lineaPedido -> lineaPedido.getCantidad() * lineaPedido.getPrecioProducto())
                .reduce(0.0, Double::sum);


        var totalItems = pedido.getLineasPedido().stream()
                .map(LineaPedido::getCantidad)
                .reduce(0, Integer::sum);


        pedido.setTotal(total);
        pedido.setTotalItems(totalItems);

        return pedido;
    }

    @Override
    @Transactional
    @CacheEvict(key = "#idPedido")
    public void delete(ObjectId idPedido) {
        log.info("Borrando pedido: " + idPedido);

        var pedidoToDelete = pedidosRepository.findById(idPedido).orElseThrow(() -> new PedidoNotFound(idPedido.toHexString()));

        returnStockPedidos(pedidoToDelete);

        pedidosRepository.deleteById(idPedido);
    }

    Pedido returnStockPedidos(Pedido pedido) {
        log.info("Retornando stock del pedido: {}", pedido);
        if (pedido.getLineasPedido() != null) {
            pedido.getLineasPedido().forEach(lineaPedido -> {
                var funko = funkoRepository.findById(lineaPedido.getIdProducto()).get();
                funko.setStock(funko.getStock() + lineaPedido.getCantidad());
                funkoRepository.save(funko);
            });
        }
        return pedido;
    }

    @Override
    @Transactional
    @CachePut(key = "#idPedido")
    public Pedido update(ObjectId idPedido, Pedido pedido) {
        log.info("Actualizando pedido con id: " + idPedido);


        var pedidoToUpdate = pedidosRepository.findById(idPedido).orElseThrow(() -> new PedidoNotFound(idPedido.toHexString()));

        returnStockPedidos(pedido);


        checkPedido(pedido);

        var pedidoToSave = reserveStockPedidos(pedido);

        pedidoToSave.setUpdatedAt(LocalDateTime.now());

        return pedidosRepository.save(pedidoToSave);

    }

    void checkPedido(Pedido pedido) {
        log.info("Comprobando pedido: {}", pedido);

        if (pedido.getLineasPedido() == null || pedido.getLineasPedido().isEmpty()) {
            throw new PedidoNotItems(pedido.getId().toHexString());
        }
        pedido.getLineasPedido().forEach(lineaPedido -> {
            var funko = funkoRepository.findById(lineaPedido.getIdProducto())
                    .orElseThrow(() -> new ProductoNotFound(lineaPedido.getIdProducto()));

            if (funko.getStock() < lineaPedido.getCantidad() && lineaPedido.getCantidad() > 0) {
                throw new ProductoNotStock(lineaPedido.getIdProducto());
            }
            if (!funko.getPrice().equals(lineaPedido.getPrecioProducto())) {
                throw new ProductoBadPrice(lineaPedido.getIdProducto());
            }
        });
    }
}
