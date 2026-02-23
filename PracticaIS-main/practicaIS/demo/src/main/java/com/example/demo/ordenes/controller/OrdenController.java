package com.example.demo.ordenes.controller;

import com.example.demo.ordenes.domain.InfoEnvio;
import com.example.demo.ordenes.domain.OrdenId;
import com.example.demo.ordenes.dto.OrdenRequest;
import com.example.demo.ordenes.dto.OrdenResponse;
import com.example.demo.ordenes.service.OrdenService;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ordenes.domain.DireccionEnvio;
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
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Operaciones para gestión de órdenes de compra")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva orden", description = "Crea una orden a partir de los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody OrdenRequest request) {
        OrdenResponse response = ordenService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/desde-carrito/{carritoId}")
    @Operation(summary = "Crear orden desde carrito", description = "Crea una orden a partir del carrito especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    public ResponseEntity<OrdenResponse> crearDesdeCarrito(
            @Parameter(description = "ID del carrito", required = true) @PathVariable UUID carritoId,
            @RequestBody DireccionEnvio direccion) {
        OrdenResponse response = ordenService.crearDesdeCarrito(new CarritoId(carritoId), direccion);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID", description = "Retorna la orden correspondiente al ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden encontrada",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<OrdenResponse> obtener(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id) {
        OrdenResponse response = ordenService.buscarPorId(new OrdenId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes", description = "Retorna una lista de todas las órdenes")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes",
            content = @Content(schema = @Schema(implementation = OrdenResponse.class)))
    public ResponseEntity<List<OrdenResponse>> listarTodas() {
        List<OrdenResponse> response = ordenService.buscarTodas();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar orden", description = "Cambia el estado de la orden a CONFIRMADA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden confirmada",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "La orden no está pendiente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> confirmar(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id,
            @RequestParam String usuario) {
        OrdenResponse response = ordenService.confirmar(new OrdenId(id), usuario);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pago")
    @Operation(summary = "Procesar pago de orden", description = "Registra el pago de la orden")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago procesado",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "La orden no está confirmada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> procesarPago(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id,
            @RequestParam String referencia) {
        OrdenResponse response = ordenService.procesarPago(new OrdenId(id), referencia);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/en-proceso")
    @Operation(summary = "Marcar orden en preparación", description = "Cambia el estado a EN_PREPARACION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden en preparación",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "El pago no ha sido procesado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> marcarEnProceso(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id) {
        OrdenResponse response = ordenService.marcarEnProceso(new OrdenId(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/enviada")
    @Operation(summary = "Marcar orden como enviada", description = "Registra la información de envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden enviada",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "La orden no está en preparación"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> marcarEnviada(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id,
            @RequestBody InfoEnvio infoEnvio) {
        OrdenResponse response = ordenService.marcarEnviada(new OrdenId(id), infoEnvio);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/entregada")
    @Operation(summary = "Marcar orden como entregada", description = "Cambia el estado a ENTREGADA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden entregada",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "La orden no está enviada o en tránsito"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> marcarEntregada(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id) {
        OrdenResponse response = ordenService.marcarEntregada(new OrdenId(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar orden", description = "Cancela la orden con un motivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden cancelada",
                    content = @Content(schema = @Schema(implementation = OrdenResponse.class))),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar (ya enviada/entregada) o motivo inválido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> cancelar(
            @Parameter(description = "ID de la orden", required = true) @PathVariable UUID id,
            @RequestParam String motivo) {
        OrdenResponse response = ordenService.cancelar(new OrdenId(id), motivo);
        return ResponseEntity.ok(response);
    }
}