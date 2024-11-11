package org.example.categoria.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaDto(
        @NotBlank(message = "El tipo de la categoría no puede ser vacío")
        String tipo,
        @NotNull(message = "Enabled enabled de la categoria no puede ser null")
        Boolean enabled
) {
}
