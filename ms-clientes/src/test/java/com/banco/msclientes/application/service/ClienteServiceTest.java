package com.banco.msclientes.application.service;

import com.banco.msclientes.domain.exception.ClienteDuplicadoException;
import com.banco.msclientes.domain.exception.ClienteNoEncontradoException;
import com.banco.msclientes.domain.model.Cliente;
import com.banco.msclientes.domain.repository.ClienteRepository;
import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteJoseLema;
    private ClienteRequestDTO requestJoseLema;

    @BeforeEach
    void setUp() {
        clienteJoseLema = new Cliente();
        clienteJoseLema.setId(1L);
        clienteJoseLema.setClienteId("jose.lema");
        clienteJoseLema.setNombre("Jose Lema");
        clienteJoseLema.setGenero("M");
        clienteJoseLema.setEdad(35);
        clienteJoseLema.setIdentificacion("1234567890");
        clienteJoseLema.setDireccion("Otavalo sn y principal");
        clienteJoseLema.setTelefono("098254785");
        clienteJoseLema.setContrasena("$2a$10$hashedPassword");
        clienteJoseLema.setEstado(true);

        requestJoseLema = ClienteRequestDTO.builder()
                .clienteId("jose.lema")
                .nombre("Jose Lema")
                .genero("M")
                .edad(35)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .contrasena("1234")
                .estado(true)
                .build();
    }

    @Test
    void listarTodos_existenClientes_retornaListaConTodosLosElementos() {
        Cliente clienteMarianela = new Cliente();
        clienteMarianela.setId(2L);
        clienteMarianela.setClienteId("marianela.montalvo");
        clienteMarianela.setNombre("Marianela Montalvo");
        clienteMarianela.setGenero("F");
        clienteMarianela.setEdad(30);
        clienteMarianela.setEstado(true);

        when(clienteRepository.findAll()).thenReturn(List.of(clienteJoseLema, clienteMarianela));

        List<ClienteResponseDTO> resultado = clienteService.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void listarTodos_repositorioVacio_retornaListaVacia() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<ClienteResponseDTO> resultado = clienteService.listarTodos();

        assertThat(resultado).isNotNull().isEmpty();
    }

    @Test
    void obtenerPorClienteId_clienteExiste_retornaDTO() {
        when(clienteRepository.findByClienteId("jose.lema")).thenReturn(Optional.of(clienteJoseLema));

        ClienteResponseDTO resultado = clienteService.obtenerPorClienteId("jose.lema");

        assertThat(resultado.getClienteId()).isEqualTo("jose.lema");
        assertThat(resultado.getNombre()).isEqualTo("Jose Lema");
    }

    @Test
    void obtenerPorClienteId_clienteNoExiste_lanzaClienteNoEncontradoException() {
        when(clienteRepository.findByClienteId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorClienteId("inexistente"))
                .isInstanceOf(ClienteNoEncontradoException.class)
                .hasMessageContaining("inexistente");
    }

    @Test
    void crear_clienteIdNuevo_retornaDTOConClienteIdCorrecto() {
        when(clienteRepository.existsByClienteId("jose.lema")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("$2a$10$hashedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteJoseLema);

        ClienteResponseDTO resultado = clienteService.crear(requestJoseLema);

        assertThat(resultado.getClienteId()).isEqualTo("jose.lema");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(passwordEncoder, times(1)).encode("1234");
    }

    @Test
    void crear_clienteIdDuplicado_lanzaClienteDuplicadoException() {
        when(clienteRepository.existsByClienteId("jose.lema")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(requestJoseLema))
                .isInstanceOf(ClienteDuplicadoException.class)
                .hasMessageContaining("jose.lema");
    }

    @Test
    void actualizar_clienteExistente_actualizaNombreCorrectamente() {
        ClienteRequestDTO dtoActualizado = ClienteRequestDTO.builder()
                .clienteId("jose.lema")
                .nombre("Jose Lema Actualizado")
                .genero("M")
                .edad(36)
                .identificacion("1234567890")
                .direccion("Nueva Direccion 456")
                .telefono("098254785")
                .contrasena("newpass")
                .estado(true)
                .build();

        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(1L);
        clienteActualizado.setClienteId("jose.lema");
        clienteActualizado.setNombre("Jose Lema Actualizado");
        clienteActualizado.setEdad(36);
        clienteActualizado.setEstado(true);

        when(clienteRepository.findByClienteId("jose.lema")).thenReturn(Optional.of(clienteJoseLema));
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$10$newHashedPass");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        ClienteResponseDTO resultado = clienteService.actualizar("jose.lema", dtoActualizado);

        assertThat(resultado.getNombre()).isEqualTo("Jose Lema Actualizado");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void actualizar_clienteInexistente_lanzaClienteNoEncontradoException() {
        when(clienteRepository.findByClienteId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar("inexistente", requestJoseLema))
                .isInstanceOf(ClienteNoEncontradoException.class)
                .hasMessageContaining("inexistente");
    }

    @Test
    void eliminar_clienteExistente_eliminaCorrectamente() {
        when(clienteRepository.findByClienteId("jose.lema")).thenReturn(Optional.of(clienteJoseLema));

        clienteService.eliminar("jose.lema");

        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_clienteIdInexistente_lanzaClienteNoEncontradoException() {
        when(clienteRepository.findByClienteId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminar("inexistente"))
                .isInstanceOf(ClienteNoEncontradoException.class)
                .hasMessageContaining("inexistente");
    }
}
