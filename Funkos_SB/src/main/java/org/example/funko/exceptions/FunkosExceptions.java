package org.example.funko.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class FunkosExceptions extends RuntimeException {
    public FunkosExceptions(String message) {
        super(message);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class FunkoNotFound extends FunkosExceptions {
        public FunkoNotFound(Long id) {
            super("Funko not found with id: " + id);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class FunkoNotFoundByName extends FunkosExceptions {
        public FunkoNotFoundByName(String name) {
            super("Funko not found with name: " + name);
        }
    }
}

