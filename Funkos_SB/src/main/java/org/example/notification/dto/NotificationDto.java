package org.example.notification.dto;

public record NotificationDto(
        Long id,
        String name,
        String description,
        String categoria,
        String imageUrl,
        Double price,
        String createdAt,
        String updatedAt
){

}