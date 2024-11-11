package org.example.notification.models;

public record Notification<T>(
        String entity,
        Tipo tipo,
        T data,
        String createdAt
) {
    public enum Tipo {
        CREATE,
        UPDATE,
        DELETE
    }
}