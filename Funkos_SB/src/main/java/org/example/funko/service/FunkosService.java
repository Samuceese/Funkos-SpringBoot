package org.example.funko.service;


import org.example.funko.models.Funko;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FunkosService {
    Page<Funko> getAllFunkos(Optional<String> name, Optional<String> categoria, Optional<Double> price, Pageable pageable);
    Funko getFunkoById(Long id);
    Funko getFunkoByName(String name);
    Funko createFunko(Funko funko);
    Funko updateFunko(Long id, Funko funko);
    Funko deleteFunko(Long id);
}
