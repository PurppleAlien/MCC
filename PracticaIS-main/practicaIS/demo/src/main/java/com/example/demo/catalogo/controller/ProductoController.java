package com.example.demo.catalogo.controller;

import com.example.demo.shared.domain.ProductoId;
import com.example.demo.catalogo.dto.ProductoRequest;
import com.example.demo.catalogo.dto.ProductoResponse;
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
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Operaciones para gestión de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto", description = "Crea un producto con los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = productoService.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Retorna el producto correspondiente al ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductoResponse> obtener(
            @Parameter(description = "ID del producto", required = true) @PathVariable UUID id) {
        ProductoResponse response = productoService.buscarProductoPorId(new ProductoId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Retorna una lista de todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista de productos",
            content = @Content(schema = @Schema(implementation = ProductoResponse.class)))
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        List<ProductoResponse> response = productoService.buscarTodosProductos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductoResponse> actualizar(
            @Parameter(description = "ID del producto", required = true) @PathVariable UUID id,
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = productoService.actualizarProducto(new ProductoId(id), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar un producto", description = "Cambia el estado del producto a activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto activado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "No se puede activar (falta imagen o precio inválido)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductoResponse> activar(
            @Parameter(description = "ID del producto", required = true) @PathVariable UUID id) {
        ProductoResponse response = productoService.activarProducto(new ProductoId(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar un producto", description = "Cambia el estado del producto a inactivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto desactivado",
                    content = @Content(schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductoResponse> desactivar(
            @Parameter(description = "ID del producto", required = true) @PathVariable UUID id) {
        ProductoResponse response = productoService.desactivarProducto(new ProductoId(id));
        return ResponseEntity.ok(response);
    }
}