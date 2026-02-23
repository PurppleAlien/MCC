package com.example.demo.catalogo.controller;

import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.dto.CategoriaRequest;
import com.example.demo.catalogo.dto.CategoriaResponse;
import com.example.demo.catalogo.service.ProductoService;
import com.example.demo.dto.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Operaciones para gestión de categorías")
public class CategoriaController {

    private final ProductoService productoService;

    public CategoriaController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoría", description = "Crea una categoría con los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = productoService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Retorna la categoría correspondiente al ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CategoriaResponse> obtener(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable UUID id) {
        CategoriaResponse response = productoService.buscarCategoriaPorId(new CategoriaId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Retorna una lista de todas las categorías")
    @ApiResponse(responseCode = "200", description = "Lista de categorías",
            content = @Content(schema = @Schema(implementation = CategoriaResponse.class)))
    public ResponseEntity<List<CategoriaResponse>> listarTodas() {
        List<CategoriaResponse> response = productoService.buscarTodasCategorias();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría", description = "Actualiza los datos de una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CategoriaResponse> actualizar(
            @Parameter(description = "ID de la categoría", required = true) @PathVariable UUID id,
            @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = productoService.actualizarCategoria(new CategoriaId(id), request);
        return ResponseEntity.ok(response);
    }
}