package com.banco.mscuentas.infrastructure.controller;

import com.banco.mscuentas.application.service.ICuentaService;
import com.banco.mscuentas.dto.ApiErrorResponse;
import com.banco.mscuentas.dto.CuentaRequestDTO;
import com.banco.mscuentas.dto.CuentaResponseDTO;
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

@Tag(name = "Cuentas", description = "CRUD de cuentas bancarias")
@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final ICuentaService cuentaService;

    public CuentaController(ICuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Listar todas las cuentas")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas")
    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(cuentaService.listarTodas());
    }

    @Operation(summary = "Obtener cuenta por número")
    @ApiResponse(responseCode = "200", description = "Cuenta encontrada")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerPorNumeroCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerPorNumeroCuenta(numeroCuenta));
    }

    @Operation(summary = "Obtener cuentas por clienteId")
    @ApiResponse(responseCode = "200", description = "Cuentas del cliente")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CuentaResponseDTO>> obtenerPorClienteId(@PathVariable String clienteId) {
        return ResponseEntity.ok(cuentaService.obtenerPorClienteId(clienteId));
    }

    @Operation(summary = "Crear una nueva cuenta")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Número de cuenta ya registrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crear(
            @Valid @RequestBody CuentaRequestDTO dto,
            UriComponentsBuilder ucb) {
        CuentaResponseDTO result = cuentaService.crear(dto);
        URI location = ucb.path("/cuentas/{numeroCuenta}")
                .buildAndExpand(result.getNumeroCuenta())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @Operation(summary = "Actualizar cuenta existente")
    @ApiResponse(responseCode = "200", description = "Cuenta actualizada")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> actualizar(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CuentaRequestDTO dto) {
        return ResponseEntity.ok(cuentaService.actualizar(numeroCuenta, dto));
    }

    @Operation(summary = "Eliminar cuenta")
    @ApiResponse(responseCode = "204", description = "Cuenta eliminada")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Cuenta tiene movimientos registrados",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroCuenta) {
        cuentaService.eliminar(numeroCuenta);
        return ResponseEntity.noContent().build();
    }
}
