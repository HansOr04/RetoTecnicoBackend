package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.exception.CuentaInactivaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.exception.MovimientoNoEncontradoException;
import com.banco.mscuentas.domain.exception.SaldoInsuficienteException;
import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.model.Movimiento;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.domain.repository.MovimientoRepository;
import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import com.banco.mscuentas.dto.ReporteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private MovimientoService movimientoService;

    private Cuenta cuentaJoseLema;
    private Cuenta cuentaMarianela;

    @BeforeEach
    void setUp() {
        cuentaJoseLema = new Cuenta();
        cuentaJoseLema.setId(1L);
        cuentaJoseLema.setNumeroCuenta("478758");
        cuentaJoseLema.setTipoCuenta("Ahorro");
        cuentaJoseLema.setSaldoInicial(BigDecimal.valueOf(2000.0));
        cuentaJoseLema.setSaldoDisponible(BigDecimal.valueOf(2000.0));
        cuentaJoseLema.setEstado(true);
        cuentaJoseLema.setClienteId("jose.lema");

        cuentaMarianela = new Cuenta();
        cuentaMarianela.setId(2L);
        cuentaMarianela.setNumeroCuenta("225487");
        cuentaMarianela.setTipoCuenta("Corriente");
        cuentaMarianela.setSaldoInicial(BigDecimal.valueOf(100.0));
        cuentaMarianela.setSaldoDisponible(BigDecimal.valueOf(100.0));
        cuentaMarianela.setEstado(true);
        cuentaMarianela.setClienteId("marianela.montalvo");
    }

    @Test
    void registrar_deposito_calculaSaldoCorrecto() {
        MovimientoRequestDTO dto = buildRequest("478758", "Deposito", BigDecimal.valueOf(600.0));

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaJoseLema);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuentaJoseLema);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(2600.0));

        ArgumentCaptor<Movimiento> movCaptor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).save(movCaptor.capture());
        assertThat(movCaptor.getValue().getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(2600.0));
        assertThat(movCaptor.getValue().getFecha()).isNotNull();

        ArgumentCaptor<Cuenta> cuentaCaptor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(cuentaCaptor.capture());
        assertThat(cuentaCaptor.getValue().getSaldoDisponible()).isEqualByComparingTo(BigDecimal.valueOf(2600.0));
    }

    @Test
    void registrar_retiro_calculaSaldoCorrecto() {
        MovimientoRequestDTO dto = buildRequest("478758", "Retiro", BigDecimal.valueOf(575.0));

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaJoseLema);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuentaJoseLema);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(1425.0));

        ArgumentCaptor<Movimiento> movCaptor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).save(movCaptor.capture());
        assertThat(movCaptor.getValue().getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(1425.0));
        assertThat(movCaptor.getValue().getFecha()).isNotNull();

        ArgumentCaptor<Cuenta> cuentaCaptor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(cuentaCaptor.capture());
        assertThat(cuentaCaptor.getValue().getSaldoDisponible()).isEqualByComparingTo(BigDecimal.valueOf(1425.0));
    }

    @Test
    void registrar_retiroExactoSaldoDisponible_dejaSaldoCero() {
        cuentaMarianela.setSaldoDisponible(BigDecimal.valueOf(540.0));
        MovimientoRequestDTO dto = buildRequest("225487", "Retiro", BigDecimal.valueOf(540.0));

        when(cuentaRepository.findByNumeroCuenta("225487")).thenReturn(Optional.of(cuentaMarianela));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaMarianela);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuentaMarianela);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void registrar_saldoInsuficiente_lanzaSaldoInsuficienteException() {
        MovimientoRequestDTO dto = buildRequest("225487", "Retiro", BigDecimal.valueOf(600.0));

        when(cuentaRepository.findByNumeroCuenta("225487")).thenReturn(Optional.of(cuentaMarianela));

        assertThatThrownBy(() -> movimientoService.registrar(dto))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo no disponible");

        verify(movimientoRepository, never()).save(any());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void registrar_cuentaInexistente_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findByNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movimientoService.registrar(buildRequest("000000", "Deposito", BigDecimal.valueOf(100.0))))
                .isInstanceOf(CuentaNoEncontradaException.class)
                .hasMessageContaining("000000");

        verify(movimientoRepository, never()).save(any());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void registrar_tipoMovimientoInvalido_lanzaIllegalArgumentException() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));

        assertThatThrownBy(() -> movimientoService.registrar(buildRequest("478758", "Transferencia", BigDecimal.valueOf(100.0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de movimiento inválido");
    }

    @Test
    void generarReporte_clienteConMovimientosEnRango_retornaListaConMovimientos() {
        Movimiento deposito = new Movimiento();
        deposito.setId(1L);
        deposito.setFecha(LocalDateTime.now());
        deposito.setTipoMovimiento("Deposito");
        deposito.setValor(BigDecimal.valueOf(600.0));
        deposito.setSaldo(BigDecimal.valueOf(700.0));
        deposito.setCuenta(cuentaMarianela);

        Movimiento retiro = new Movimiento();
        retiro.setId(2L);
        retiro.setFecha(LocalDateTime.now());
        retiro.setTipoMovimiento("Retiro");
        retiro.setValor(BigDecimal.valueOf(540.0));
        retiro.setSaldo(BigDecimal.valueOf(160.0));
        retiro.setCuenta(cuentaMarianela);

        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now().plusDays(1);

        when(movimientoRepository.findByCuenta_ClienteIdAndFechaBetween("marianela.montalvo", inicio, fin))
                .thenReturn(List.of(deposito, retiro));

        List<ReporteDTO> resultado = movimientoService.generarReporte("marianela.montalvo", inicio, fin);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getMovimiento()).isPositive();
        assertThat(resultado.get(1).getMovimiento()).isNegative();
    }

    @Test
    void generarReporte_sinMovimientosEnRango_retornaListaVacia() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fin = LocalDateTime.now().minusDays(1);

        when(movimientoRepository.findByCuenta_ClienteIdAndFechaBetween("jose.lema", inicio, fin))
                .thenReturn(List.of());

        List<ReporteDTO> resultado = movimientoService.generarReporte("jose.lema", inicio, fin);

        assertThat(resultado).isNotNull().isEmpty();
    }

    @Test
    void registrar_valorDecimal_saldoResultadoEsExacto() {
        // Verifica que no hay errores de punto flotante binario:
        // 100.10 - 0.10 debe ser exactamente 100.00, no 99.99999999999999
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("TEST-001");
        cuenta.setSaldoDisponible(BigDecimal.valueOf(100.10));

        when(cuentaRepository.findByNumeroCuenta("TEST-001")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.save(any())).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuenta);
            return m;
        });
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MovimientoRequestDTO dto = MovimientoRequestDTO.builder()
                .numeroCuenta("TEST-001")
                .tipoMovimiento("Retiro")
                .valor(BigDecimal.valueOf(0.10))
                .build();

        MovimientoResponseDTO response = movimientoService.registrar(dto);

        // Con BigDecimal: 100.10 - 0.10 = exactamente 100.00
        // Con Double esto fallaría: 100.10 - 0.10 = 99.99999999999999
        assertThat(response.getSaldo()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void listarTodos_retornaListaConMovimientos() {
        Movimiento mov = new Movimiento();
        mov.setId(1L);
        mov.setFecha(LocalDateTime.now());
        mov.setTipoMovimiento("Deposito");
        mov.setValor(BigDecimal.valueOf(100.0));
        mov.setSaldo(BigDecimal.valueOf(2100.0));
        mov.setCuenta(cuentaJoseLema);

        when(movimientoRepository.findAll()).thenReturn(List.of(mov));

        List<MovimientoResponseDTO> resultado = movimientoService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNumeroCuenta()).isEqualTo("478758");
    }

    @Test
    void registrar_cuentaInactiva_lanzaCuentaInactivaException() {
        cuentaJoseLema.setEstado(false);
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));

        assertThatThrownBy(() -> movimientoService.registrar(buildRequest("478758", "Deposito", BigDecimal.valueOf(100.0))))
                .isInstanceOf(CuentaInactivaException.class)
                .hasMessageContaining("478758");

        verify(movimientoRepository, never()).save(any());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void actualizar_lanzaUnsupportedOperationException() {
        assertThatThrownBy(() -> movimientoService.actualizar(1L, buildRequest("478758", "Deposito", BigDecimal.valueOf(100.0))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void eliminar_movimientoExistente_eliminaCorrectamente() {
        when(movimientoRepository.existsById(1L)).thenReturn(true);

        movimientoService.eliminar(1L);

        verify(movimientoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_movimientoNoEncontrado_lanzaMovimientoNoEncontradoException() {
        when(movimientoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movimientoService.eliminar(99L))
                .isInstanceOf(MovimientoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(movimientoRepository, never()).deleteById(any());
    }

    private MovimientoRequestDTO buildRequest(String numeroCuenta, String tipo, BigDecimal valor) {
        return MovimientoRequestDTO.builder()
                .numeroCuenta(numeroCuenta)
                .tipoMovimiento(tipo)
                .valor(valor)
                .build();
    }
}
