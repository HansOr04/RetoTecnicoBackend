package com.banco.mscuentas.integration;

import com.banco.mscuentas.dto.ApiErrorResponse;
import com.banco.mscuentas.dto.CuentaRequestDTO;
import com.banco.mscuentas.dto.CuentaResponseDTO;
import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import com.banco.mscuentas.dto.ReporteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MovimientoIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    private CuentaRequestDTO buildCuentaRequest(String numeroCuenta, BigDecimal saldo) {
        return CuentaRequestDTO.builder()
                .numeroCuenta(numeroCuenta)
                .tipoCuenta("Ahorro")
                .saldoInicial(saldo)
                .estado(true)
                .clienteId("jose.lema")
                .build();
    }

    private MovimientoRequestDTO buildMovimientoRequest(String numeroCuenta, String tipo, BigDecimal valor) {
        return MovimientoRequestDTO.builder()
                .numeroCuenta(numeroCuenta)
                .tipoMovimiento(tipo)
                .valor(valor)
                .build();
    }

    @Test
    void registrarRetiro_cuentaExistente_actualizaSaldoYRetorna201() {
        restClient.post().uri("/cuentas")
                .body(buildCuentaRequest("478758", BigDecimal.valueOf(2000.0)))
                .retrieve().toBodilessEntity();

        ResponseEntity<MovimientoResponseDTO> response = restClient.post()
                .uri("/movimientos")
                .body(buildMovimientoRequest("478758", "Retiro", BigDecimal.valueOf(575.0)))
                .retrieve()
                .toEntity(MovimientoResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(1425.0));
    }

    @Test
    void registrarRetiro_saldoInsuficiente_retorna400ConMensajeExacto() {
        restClient.post().uri("/cuentas")
                .body(buildCuentaRequest("495878", BigDecimal.ZERO))
                .retrieve().toBodilessEntity();

        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.post().uri("/movimientos")
                        .body(buildMovimientoRequest("495878", "Retiro", BigDecimal.valueOf(150.0)))
                        .retrieve().toBodilessEntity());

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getResponseBodyAsString()).contains("Saldo no disponible");
    }

    @Test
    void generarReporte_clienteConMovimientos_retornaMovimientosConSignoCorrecto() {
        restClient.post().uri("/cuentas")
                .body(buildCuentaRequest("585545", BigDecimal.valueOf(1000.0)))
                .retrieve().toBodilessEntity();

        restClient.post().uri("/movimientos")
                .body(buildMovimientoRequest("585545", "Deposito", BigDecimal.valueOf(500.0)))
                .retrieve().toBodilessEntity();
        restClient.post().uri("/movimientos")
                .body(buildMovimientoRequest("585545", "Retiro", BigDecimal.valueOf(200.0)))
                .retrieve().toBodilessEntity();

        String hoy = LocalDate.now().toString();
        List<ReporteDTO> reporte = restClient.get()
                .uri("/reportes?clienteId=jose.lema&fechaInicio={f}&fechaFin={f}", hoy, hoy)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ReporteDTO>>() {});

        assertThat(reporte).isNotNull().hasSize(2);
        assertThat(reporte.stream().anyMatch(r -> r.getMovimiento().compareTo(BigDecimal.ZERO) > 0)).isTrue();
        assertThat(reporte.stream().anyMatch(r -> r.getMovimiento().compareTo(BigDecimal.ZERO) < 0)).isTrue();
    }

    @Test
    void actualizar_movimiento_retorna405() {
        restClient.post().uri("/cuentas")
                .body(buildCuentaRequest("478758", BigDecimal.valueOf(2000.0)))
                .retrieve().toBodilessEntity();

        ResponseEntity<MovimientoResponseDTO> created = restClient.post()
                .uri("/movimientos")
                .body(buildMovimientoRequest("478758", "Deposito", BigDecimal.valueOf(100.0)))
                .retrieve()
                .toEntity(MovimientoResponseDTO.class);

        Long movId = created.getBody().getId();

        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.put().uri("/movimientos/{id}", movId)
                        .body(buildMovimientoRequest("478758", "Deposito", BigDecimal.valueOf(200.0)))
                        .retrieve().toBodilessEntity());

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void registrar_cuentaInactiva_retorna422() {
        restClient.post().uri("/cuentas")
                .body(buildCuentaRequest("111111", BigDecimal.valueOf(500.0)))
                .retrieve().toBodilessEntity();

        CuentaRequestDTO inactiva = CuentaRequestDTO.builder()
                .numeroCuenta("111111")
                .tipoCuenta("Ahorro")
                .saldoInicial(BigDecimal.valueOf(500.0))
                .estado(false)
                .clienteId("jose.lema")
                .build();
        restClient.put().uri("/cuentas/111111").body(inactiva).retrieve().toBodilessEntity();

        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.post().uri("/movimientos")
                        .body(buildMovimientoRequest("111111", "Deposito", BigDecimal.valueOf(100.0)))
                        .retrieve().toBodilessEntity());

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    void eliminar_movimientoNoEncontrado_retorna404() {
        HttpClientErrorException ex = catchThrowableOfType(
                HttpClientErrorException.class,
                () -> restClient.delete().uri("/movimientos/99999")
                        .retrieve().toBodilessEntity());

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
