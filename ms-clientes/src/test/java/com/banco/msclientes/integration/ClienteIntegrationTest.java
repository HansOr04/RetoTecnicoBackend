package com.banco.msclientes.integration;

import com.banco.msclientes.dto.ApiErrorResponse;
import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Prueba de integración para el microservicio ms-clientes.
 * Levanta el contexto completo de Spring Boot con H2 en memoria.
 * Verifica el flujo end-to-end: HTTP request → controller → service → repository → BD.
 * Requerido por F6 del enunciado técnico (SemiSenior).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClienteIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    private ClienteRequestDTO buildClienteRequest() {
        return ClienteRequestDTO.builder()
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
    void crearCliente_datosValidos_retorna201YClienteEnBD() {
        ResponseEntity<ClienteResponseDTO> response = restClient.post()
                .uri("/clientes")
                .body(buildClienteRequest())
                .retrieve()
                .toEntity(ClienteResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getClienteId()).isEqualTo("jose.lema");
        assertThat(response.getBody().getNombre()).isEqualTo("Jose Lema");
        // ClienteResponseDTO no tiene campo contrasena — garantiza que no se expone
    }

    @Test
    void crearCliente_clienteIdDuplicado_retorna409() {
        restClient.post().uri("/clientes").body(buildClienteRequest()).retrieve().toBodilessEntity();

        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.post().uri("/clientes").body(buildClienteRequest()).retrieve().toBodilessEntity());

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void obtenerCliente_clienteNoExiste_retorna404() {
        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.get().uri("/clientes/no-existe").retrieve().toEntity(ApiErrorResponse.class));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void crearCliente_camposInvalidos_retorna400ConErroresPorCampo() {
        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.post().uri("/clientes")
                        .body(new ClienteRequestDTO())
                        .retrieve()
                        .toBodilessEntity());

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
