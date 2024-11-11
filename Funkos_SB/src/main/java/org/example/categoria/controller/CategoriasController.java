package org.example.categoria.controller;

import jakarta.validation.Valid;
import org.example.categoria.dto.CategoriaDto;
import org.example.categoria.mapper.CategoriaMapper;
import org.example.categoria.models.Categoria;
import org.example.categoria.service.CategoriasService;
import org.example.utils.pageresponse.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("${api.path}/${api.version}/categorias")
public class CategoriasController {
    private final Logger logger = LoggerFactory.getLogger(CategoriasController.class);
    private final CategoriasService categoriasService;
    private final CategoriaMapper categoriaMapper;
    @Autowired
    public CategoriasController(CategoriasService categoriasService, CategoriaMapper categoriaMapper) {
        this.categoriasService = categoriasService;
        this.categoriaMapper = categoriaMapper;
    }
    @GetMapping
    public ResponseEntity<PageResponse<Categoria>>getAll(
            @RequestParam(required = false) Optional<String> tipo,
            @RequestParam(required = false) Optional<Boolean> enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        logger.info("Obteniendo categorias");
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(PageResponse.of(categoriasService.getCategorias(tipo, enabled, pageable), sortBy, direction));
    }

    @GetMapping("{id}")
    public ResponseEntity<Categoria> getById(@PathVariable Long id){
        logger.info("Obteniendo categoria por id {}", id);
        var result = categoriasService.getById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("tipo/{string}")
    public ResponseEntity<Categoria> getByTipo(@PathVariable String string){
        logger.info("Obteniendo categoria por tipo {}", string);
        var result = categoriasService.getByTipo(string);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Categoria> create(@Valid @RequestBody CategoriaDto categoriaDto){
        logger.info("Creando categoria");
        var result = categoriasService.create(categoriaMapper.fromCategoriaDto(categoriaDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("{id}")
    public ResponseEntity<Categoria> update(@PathVariable Long id, @RequestBody CategoriaDto categoriaDto){
        logger.info("Actualizando categoria por id {}", id);
        var result = categoriasService.update(id, categoriaMapper.fromCategoriaDto(categoriaDto));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        logger.info("Borrando categoria por id {}", id);
        categoriasService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
