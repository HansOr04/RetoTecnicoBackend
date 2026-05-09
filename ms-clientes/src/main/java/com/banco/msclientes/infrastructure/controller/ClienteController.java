package com.banco.msclientes.infrastructure.controller;

import com.banco.msclientes.application.service.IClienteService;
import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorClienteId(@PathVariable String clienteId) {
        return ResponseEntity.ok(clienteService.obtenerPorClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(dto));
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable String clienteId,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(clienteId, dto));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<String> eliminar(@PathVariable String clienteId) {
        clienteService.eliminar(clienteId);
        return ResponseEntity.ok("Cliente eliminado correctamente");
    }
}
