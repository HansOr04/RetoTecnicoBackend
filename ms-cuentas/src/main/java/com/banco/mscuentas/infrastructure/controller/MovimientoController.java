package com.banco.mscuentas.infrastructure.controller;

import com.banco.mscuentas.application.service.IMovimientoService;
import com.banco.mscuentas.dto.ApiErrorResponse;
import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Movimientos", description = "Registro de movimientos bancarios (depósitos y retiros)")
@RestController
@RequestMapping("/movimientos")
public class MovimientoController {

    private final IMovimientoService movimientoService;

    public MovimientoController(IMovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @Operation(summary = "Listar todos los movimientos")
    @ApiResponse(responseCode = "200", description = "Lista de movimientos")
    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(movimientoService.listarTodos());
    }

    @Operation(summary = "Registrar un movimiento (depósito o retiro)")
    @ApiResponse(responseCode = "201", description = "Movimiento registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o saldo insuficiente",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Cuenta inactiva",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> registrar(
            @Valid @RequestBody MovimientoRequestDTO dto,
            UriComponentsBuilder ucb) {
        MovimientoResponseDTO result = movimientoService.registrar(dto);
        URI location = ucb.path("/movimientos/{id}")
                .buildAndExpand(result.getId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @Operation(summary = "Actualizar movimiento (no soportado — los movimientos son inmutables)")
    @ApiResponse(responseCode = "405", description = "Operación no permitida",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoRequestDTO dto) {
        return ResponseEntity.ok(movimientoService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar movimiento por ID")
    @ApiResponse(responseCode = "204", description = "Movimiento eliminado")
    @ApiResponse(responseCode = "404", description = "Movimiento no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
