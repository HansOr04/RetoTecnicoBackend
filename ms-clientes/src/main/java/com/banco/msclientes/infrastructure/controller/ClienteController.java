package com.banco.msclientes.infrastructure.controller;

import com.banco.msclientes.application.service.IClienteService;
import com.banco.msclientes.dto.ApiErrorResponse;
import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;
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

@Tag(name = "Clientes", description = "CRUD de clientes bancarios")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Listar todos los clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @Operation(summary = "Obtener cliente por clienteId")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorClienteId(@PathVariable String clienteId) {
        return ResponseEntity.ok(clienteService.obtenerPorClienteId(clienteId));
    }

    @Operation(summary = "Crear un nuevo cliente")
    @ApiResponse(responseCode = "201", description = "Cliente creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "clienteId ya registrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(
            @Valid @RequestBody ClienteRequestDTO dto,
            UriComponentsBuilder ucb) {
        ClienteResponseDTO result = clienteService.crear(dto);
        URI location = ucb.path("/clientes/{clienteId}")
                .buildAndExpand(result.getClienteId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @Operation(summary = "Actualizar cliente existente")
    @ApiResponse(responseCode = "200", description = "Cliente actualizado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable String clienteId,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(clienteId, dto));
    }

    @Operation(summary = "Eliminar cliente")
    @ApiResponse(responseCode = "204", description = "Cliente eliminado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
        clienteService.eliminar(clienteId);
        return ResponseEntity.noContent().build();
    }
}
