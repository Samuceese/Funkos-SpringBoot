package org.example.funko.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class Descripcion {
    @JsonProperty("descripcion")
    private String descripcion;
    @JsonProperty("descriptionUpdatedAt")
    private LocalDateTime DescriptionUpdatedAt = LocalDateTime.now();

    public Descripcion() {}

    public Descripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        this.DescriptionUpdatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDescriptionUpdatedAt() {
        return DescriptionUpdatedAt;
    }
}
