package com.banco.msclientes.application.service;

import com.banco.msclientes.domain.model.Cliente;
import com.banco.msclientes.domain.repository.ClienteRepository;
import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void debeRetornarTodosLosClientes() {
        Cliente c1 = clienteConId("CLI001", 1L);
        Cliente c2 = clienteConId("CLI002", 2L);
        when(clienteRepository.findAll()).thenReturn(List.of(c1, c2));

        List<ClienteResponseDTO> resultado = clienteService.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void debeCrearClienteCorrectamente() {
        ClienteRequestDTO dto = buildRequest("CLI001");
        Cliente entidadGuardada = clienteConId("CLI001", 1L);

        when(clienteRepository.existsByClienteId("CLI001")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(entidadGuardada);

        ClienteResponseDTO resultado = clienteService.crear(dto);

        assertThat(resultado.getClienteId()).isEqualTo("CLI001");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void debeLanzarExcepcionSiClienteIdYaExiste() {
        when(clienteRepository.existsByClienteId("CLI001")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(buildRequest("CLI001")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El clienteId ya está registrado");
    }

    @Test
    void debeLanzarExcepcionSiClienteNoExisteAlEliminar() {
        when(clienteRepository.findByClienteId("CLI999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminar("CLI999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cliente no encontrado");
    }

    private Cliente clienteConId(String clienteId, Long id) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setClienteId(clienteId);
        c.setNombre("Juan Perez");
        c.setGenero("M");
        c.setEdad(30);
        c.setIdentificacion("12345678");
        c.setDireccion("Av. Lima 123");
        c.setTelefono("999888777");
        c.setContrasena("pass123");
        c.setEstado(true);
        return c;
    }

    private ClienteRequestDTO buildRequest(String clienteId) {
        return ClienteRequestDTO.builder()
                .clienteId(clienteId)
                .nombre("Juan Perez")
                .genero("M")
                .edad(30)
                .identificacion("12345678")
                .direccion("Av. Lima 123")
                .telefono("999888777")
                .contrasena("pass123")
                .estado(true)
                .build();
    }
}
