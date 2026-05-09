package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        cuentaJoseLema.setSaldoInicial(2000.0);
        cuentaJoseLema.setSaldoDisponible(2000.0);
        cuentaJoseLema.setEstado(true);
        cuentaJoseLema.setClienteId("jose.lema");

        cuentaMarianela = new Cuenta();
        cuentaMarianela.setId(2L);
        cuentaMarianela.setNumeroCuenta("225487");
        cuentaMarianela.setTipoCuenta("Corriente");
        cuentaMarianela.setSaldoInicial(100.0);
        cuentaMarianela.setSaldoDisponible(100.0);
        cuentaMarianela.setEstado(true);
        cuentaMarianela.setClienteId("marianela.montalvo");
    }

    @Test
    void registrar_deposito_calculaSaldoCorrecto() {
        MovimientoRequestDTO dto = buildRequest("478758", "Deposito", 600.0);

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaJoseLema);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuentaJoseLema);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualTo(2600.0);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void registrar_retiro_calculaSaldoCorrecto() {
        MovimientoRequestDTO dto = buildRequest("478758", "Retiro", 575.0);

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaJoseLema);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuentaJoseLema);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualTo(1425.0);
    }

    @Test
    void registrar_retiroExactoSaldoDisponible_dejaSaldoCero() {
        cuentaMarianela.setSaldoDisponible(540.0);
        MovimientoRequestDTO dto = buildRequest("225487", "Retiro", 540.0);

        when(cuentaRepository.findByNumeroCuenta("225487")).thenReturn(Optional.of(cuentaMarianela));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaMarianela);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuentaMarianela);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualTo(0.0);
    }

    @Test
    void registrar_saldoInsuficiente_lanzaSaldoInsuficienteException() {
        MovimientoRequestDTO dto = buildRequest("225487", "Retiro", 600.0);

        when(cuentaRepository.findByNumeroCuenta("225487")).thenReturn(Optional.of(cuentaMarianela));

        assertThatThrownBy(() -> movimientoService.registrar(dto))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo no disponible");
    }

    @Test
    void registrar_cuentaInexistente_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findByNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movimientoService.registrar(buildRequest("000000", "Deposito", 100.0)))
                .isInstanceOf(CuentaNoEncontradaException.class)
                .hasMessageContaining("000000");
    }

    @Test
    void registrar_tipoMovimientoInvalido_lanzaIllegalArgumentException() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));

        assertThatThrownBy(() -> movimientoService.registrar(buildRequest("478758", "Transferencia", 100.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de movimiento inválido");
    }

    @Test
    void generarReporte_clienteConMovimientosEnRango_retornaListaConMovimientos() {
        Movimiento deposito = new Movimiento();
        deposito.setId(1L);
        deposito.setFecha(LocalDateTime.now());
        deposito.setTipoMovimiento("Deposito");
        deposito.setValor(600.0);
        deposito.setSaldo(700.0);
        deposito.setCuenta(cuentaMarianela);

        Movimiento retiro = new Movimiento();
        retiro.setId(2L);
        retiro.setFecha(LocalDateTime.now());
        retiro.setTipoMovimiento("Retiro");
        retiro.setValor(540.0);
        retiro.setSaldo(160.0);
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

    private MovimientoRequestDTO buildRequest(String numeroCuenta, String tipo, double valor) {
        return MovimientoRequestDTO.builder()
                .numeroCuenta(numeroCuenta)
                .tipoMovimiento(tipo)
                .valor(valor)
                .build();
    }
}
