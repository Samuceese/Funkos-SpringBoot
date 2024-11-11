package org.example.funko.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import org.example.categoria.models.Categoria;
import org.example.funko.exceptions.FunkosExceptions;
import org.example.funko.models.Funko;
import org.example.funko.repository.FunkoRepository;
import org.example.notification.config.WebSocketConfig;
import org.example.notification.config.WebSocketHandler;
import org.example.notification.dto.NotificationDto;
import org.example.notification.mapper.NotificationMapper;
import org.example.notification.models.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@CacheConfig(cacheNames = {"Funkos"})
@Service
public class FunkosServiceImpl implements FunkosService {
    private final Logger logger = LoggerFactory.getLogger(FunkosServiceImpl.class);
    private final FunkoRepository funkoRepository;
    private final WebSocketConfig webSocketConfig;
    private final ObjectMapper mapper;
    private final NotificationMapper notificationMapper;
    private WebSocketHandler websocketService;
    @Autowired
    public FunkosServiceImpl(FunkoRepository funkoRepository, WebSocketConfig webSocketConfig, NotificationMapper notificationMapper) {
        this.funkoRepository = funkoRepository;
        this.webSocketConfig = webSocketConfig;
        mapper = new ObjectMapper();
        this.notificationMapper = notificationMapper;
        websocketService = webSocketConfig.webSocketFunkosHandler();
    }


    @Override
    public Page<Funko> getAllFunkos(Optional<String> name, Optional<String> categoria, Optional<Double> price, Pageable pageable) {

        Specification<Funko> specNameFunko = (root, query, criteriaBuilder) ->
                name.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + n + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Funko> specCategoriaFunko = (root, query, criteriaBuilder) ->
                categoria.map(c -> {
                    Join<Funko, Categoria> categoriaJoin = root.join("categoria");
                    return criteriaBuilder.like(criteriaBuilder.lower(categoriaJoin.get("tipo")), "%" + c + "%");
                }).orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Funko> specPriceFunko = (root, query, criteriaBuilder) ->
                price.map(p -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), p))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));


        Specification<Funko> criterio = Specification.where(specNameFunko)
                .and(specCategoriaFunko)
                .and(specPriceFunko);
        return funkoRepository.findAll(criterio, pageable);
    }

    @Override
    public Funko getFunkoById(Long id) {
        logger.info("Obteniendo funko por id {}", id);
        return funkoRepository.findById(id).orElseThrow(() -> new FunkosExceptions.FunkoNotFound(id));
    }

    @Override
    public Funko getFunkoByName(String name) {
        logger.info("Obteniendo funko por nombre {}", name);
        var res =  funkoRepository.findByName(name);
        if(res == null){
            throw new FunkosExceptions.FunkoNotFoundByName(name);
        }
        return res;
    }

    @Override
    @CachePut(key = "#result.id")
    public Funko createFunko(Funko funko) {
        logger.info("Creando funko");
        onChange(Notification.Tipo.CREATE, funko);
        return funkoRepository.save(funko);
    }

    @Override
    @CachePut(key = "#result.id")
    public Funko updateFunko(Long id, Funko funko) {
        logger.info("Actualizando persona con id {}", id);
        var res = funkoRepository.findById(id).orElseThrow( () -> new FunkosExceptions.FunkoNotFound(id));
        res.setName(funko.getName());
        res.setPrice(funko.getPrice());
        res.setCategoria(funko.getCategoria());
        res.setDescripcion(funko.getDescripcion());
        res.setImagen(funko.getImagen());
        onChange(Notification.Tipo.UPDATE, res);
        return funkoRepository.save(res);
    }

    @Override
    @CacheEvict(key = "#result.id")
    public Funko deleteFunko(Long id) {
        logger.info("Borrando persona con id {}", id);
        Funko funko = funkoRepository.findById(id).orElseThrow(() -> new FunkosExceptions.FunkoNotFound(id));
        funkoRepository.deleteById(id);
        onChange(Notification.Tipo.DELETE, funko);
        return funko;
    }

    void onChange(Notification.Tipo tipo, Funko data){
        logger.info("Servicio de funkos onChange con tipo: " +  tipo + " y datos: " + data);
        if(websocketService == null){
            logger.warn("No se ha podido enviar la notificación a los clientes ws, no se ha encontrado el servicio");
            websocketService = this.webSocketConfig.webSocketFunkosHandler();
        }
        try {
            Notification<NotificationDto> notification = new Notification<>(
                    "FUNKOS",
                    tipo,
                    NotificationMapper.toFunkoNotificationDto(data),
                    LocalDateTime.now().toString()
            );
            String json = mapper.writeValueAsString((notification));
            logger.info("Enviando mensaje a los clientes ws");
            Thread senderThread = new Thread(() ->{
                try{
                    websocketService.sendMessage(json);
                }catch (Exception e){
                    logger.error("Error al enviar el mensaje a través del servicio Websocket " , e);
                }
            });
            senderThread.start();
        }catch (JsonProcessingException e){
            logger.error("Error al convertir la notificación a JSON", e);
        }

    }
    public void setWebSocketService(WebSocketHandler webSocketHandlerMock) {
        this.websocketService = webSocketHandlerMock;
    }
}
