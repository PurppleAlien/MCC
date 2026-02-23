package com.example.demo.ventas.controller;

import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.ventas.domain.Carrito;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.dto.AgregarProductoRequest;
import com.example.demo.ventas.service.CarritoService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/carritos")
@Tag(name = "Carrito", description = "Operaciones para gestión del carrito de compras")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo carrito", description = "Crea un carrito para un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carrito creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "400", description = "ID de cliente inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Carrito> crear(
            @Parameter(description = "ID del cliente", required = true) @RequestParam UUID clienteId) {
        Carrito carrito = carritoService.crear(new ClienteId(clienteId));
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener carrito por ID", description = "Retorna el carrito correspondiente al ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Carrito> obtenerCarrito(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id) {
        Carrito carrito = carritoService.obtenerCarrito(new CarritoId(id));
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/{id}/productos")
    @Operation(summary = "Agregar producto al carrito", description = "Agrega un producto al carrito especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto agregado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Carrito o producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Carrito> agregarProducto(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id,
            @Valid @RequestBody AgregarProductoRequest request) {
        Carrito carrito = carritoService.agregarProducto(
                new CarritoId(id),
                new ProductoId(request.getProductoId()),
                request.getCantidad()
        );
        return ResponseEntity.ok(carrito);
    }

    @PatchMapping("/{id}/productos/{productoId}")
    @Operation(summary = "Modificar cantidad de un producto en el carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad modificada",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Carrito o producto no encontrado")
    })
    public ResponseEntity<Carrito> modificarCantidad(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id,
            @Parameter(description = "ID del producto", required = true) @PathVariable UUID productoId,
            @Parameter(description = "Nueva cantidad", required = true) @RequestParam int nuevaCantidad) {
        Carrito carrito = carritoService.modificarCantidad(
                new CarritoId(id),
                new ProductoId(productoId),
                nuevaCantidad
        );
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/{id}/productos/{productoId}")
    @Operation(summary = "Eliminar un producto del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "404", description = "Carrito o producto no encontrado")
    })
    public ResponseEntity<Carrito> eliminarProducto(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id,
            @Parameter(description = "ID del producto", required = true) @PathVariable UUID productoId) {
        Carrito carrito = carritoService.eliminarProducto(
                new CarritoId(id),
                new ProductoId(productoId)
        );
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/{id}/vaciar")
    @Operation(summary = "Vaciar el carrito", description = "Elimina todos los productos del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito vaciado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    public ResponseEntity<Carrito> vaciarCarrito(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id) {
        Carrito carrito = carritoService.vaciar(new CarritoId(id));
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/{id}/checkout/iniciar")
    @Operation(summary = "Iniciar checkout", description = "Cambia el estado del carrito a EN_CHECKOUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checkout iniciado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "400", description = "El carrito no está activo o está vacío"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    public ResponseEntity<Carrito> iniciarCheckout(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id) {
        Carrito carrito = carritoService.iniciarCheckout(new CarritoId(id));
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/{id}/checkout/completar")
    @Operation(summary = "Completar checkout", description = "Cambia el estado del carrito a COMPLETADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checkout completado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "400", description = "El carrito no está en checkout"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    public ResponseEntity<Carrito> completarCheckout(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id) {
        Carrito carrito = carritoService.completarCheckout(new CarritoId(id));
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/{id}/checkout/abandonar")
    @Operation(summary = "Abandonar checkout", description = "Cambia el estado del carrito a ABANDONADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checkout abandonado",
                    content = @Content(schema = @Schema(implementation = Carrito.class))),
            @ApiResponse(responseCode = "400", description = "El carrito no está en checkout"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    public ResponseEntity<Carrito> abandonarCheckout(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID id) {
        Carrito carrito = carritoService.abandonar(new CarritoId(id));
        return ResponseEntity.ok(carrito);
    }
}