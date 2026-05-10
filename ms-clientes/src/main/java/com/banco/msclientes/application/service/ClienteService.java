package com.banco.msclientes.application.service;

import com.banco.msclientes.domain.exception.ClienteDuplicadoException;
import com.banco.msclientes.domain.exception.ClienteNoEncontradoException;
import com.banco.msclientes.domain.model.Cliente;
import com.banco.msclientes.domain.repository.ClienteRepository;
import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link IClienteService}.
 * Aplica hash BCrypt a la contraseña antes de persistir.
 * Nunca expone la contraseña en los DTOs de respuesta.
 */
@Slf4j
@Service
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorClienteId(String clienteId) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        log.info("Creando cliente con clienteId: {}", dto.getClienteId());
        if (clienteRepository.existsByClienteId(dto.getClienteId())) {
            log.warn("Intento de crear cliente con clienteId duplicado: {}", dto.getClienteId());
            throw new ClienteDuplicadoException(dto.getClienteId());
        }
        Cliente cliente = Cliente.builder()
                .clienteId(dto.getClienteId())
                .nombre(dto.getNombre())
                .genero(dto.getGenero())
                .edad(dto.getEdad())
                .identificacion(dto.getIdentificacion())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                .estado(dto.getEstado())
                .build();
        ClienteResponseDTO response = mapToResponse(clienteRepository.save(cliente));
        log.info("Cliente creado exitosamente con clienteId: {}", response.getClienteId());
        return response;
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(String clienteId, ClienteRequestDTO dto) {
        log.info("Actualizando cliente con clienteId: {}", clienteId);
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        cliente.setEstado(dto.getEstado());
        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(String clienteId) {
        log.info("Eliminando cliente con clienteId: {}", clienteId);
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        clienteRepository.deleteById(cliente.getId());
    }

    private ClienteResponseDTO mapToResponse(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .identificacion(cliente.getIdentificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .build();
    }
}
