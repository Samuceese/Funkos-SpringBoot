package org.example.notification.mapper;

import org.example.funko.models.Funko;
import org.example.notification.dto.NotificationDto;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public static NotificationDto toFunkoNotificationDto(Funko funko){
        return new NotificationDto(
                funko.getId(),
                funko.getName(),
                funko.getDescripcion().getDescripcion(),
                funko.getCategoria().getTipo(),
                funko.getImagen(),
                funko.getPrice(),
                funko.getCreatedAt().toString(),
                funko.getUpdatedAt().toString()
        );
    }

}