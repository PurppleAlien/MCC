package com.uamishop.ordenes.ordenes.controller;

import com.uamishop.ordenes.ordenes.domain.InfoEnvio;
import com.uamishop.ordenes.ordenes.domain.OrdenId;
import com.uamishop.ordenes.ordenes.dto.OrdenRequest;
import com.uamishop.ordenes.ordenes.dto.OrdenResponse;
import com.uamishop.ordenes.ordenes.service.OrdenService;
import com.uamishop.ordenes.dto.ApiError;
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
@RequestMapping("/api/v1/ordenes")
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
    public ResponseEntity<OrdenResponse> confirmar(
            @PathVariable UUID id,
            @RequestParam String usuario) {
        OrdenResponse response = ordenService.confirmar(new OrdenId(id), usuario);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pago")
    @Operation(summary = "Procesar pago de orden", description = "Registra el pago de la orden")
    public ResponseEntity<OrdenResponse> procesarPago(
            @PathVariable UUID id,
            @RequestParam String referencia) {
        OrdenResponse response = ordenService.procesarPago(new OrdenId(id), referencia);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/en-proceso")
    @Operation(summary = "Marcar orden en preparación")
    public ResponseEntity<OrdenResponse> marcarEnProceso(
            @PathVariable UUID id) {
        OrdenResponse response = ordenService.marcarEnProceso(new OrdenId(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/enviada")
    @Operation(summary = "Marcar orden como enviada")
    public ResponseEntity<OrdenResponse> marcarEnviada(
            @PathVariable UUID id,
            @RequestBody InfoEnvio infoEnvio) {
        OrdenResponse response = ordenService.marcarEnviada(new OrdenId(id), infoEnvio);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/entregada")
    @Operation(summary = "Marcar orden como entregada")
    public ResponseEntity<OrdenResponse> marcarEntregada(
            @PathVariable UUID id) {
        OrdenResponse response = ordenService.marcarEntregada(new OrdenId(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar orden")
    public ResponseEntity<OrdenResponse> cancelar(
            @PathVariable UUID id,
            @RequestParam String motivo) {
        OrdenResponse response = ordenService.cancelar(new OrdenId(id), motivo);
        return ResponseEntity.ok(response);
    }
}