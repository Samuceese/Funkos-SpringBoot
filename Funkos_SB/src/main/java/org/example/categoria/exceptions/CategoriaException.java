package org.example.categoria.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class CategoriaException extends RuntimeException {
    public CategoriaException(String message) {
        super(message);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class CategoriaNotFound extends CategoriaException {
        public CategoriaNotFound(Long id) {
            super("Categoría no encontrada con id: " + id);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class CategoriaNotFoundByTipo extends CategoriaException {
        public CategoriaNotFoundByTipo(String string) {
            super("Categoría no encontrada con id: " + string);
        }
    }
}
