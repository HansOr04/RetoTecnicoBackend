package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.model.Movimiento;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.domain.repository.MovimientoRepository;
import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    @Test
    void debeRegistrarDepositoCorrectamente() {
        Cuenta cuenta = cuentaConSaldo("478758", 100.0);
        MovimientoRequestDTO dto = buildRequest("478758", "Deposito", 600.0);

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuenta);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualTo(700.0);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void debeRegistrarRetiroCorrectamente() {
        Cuenta cuenta = cuentaConSaldo("478758", 540.0);
        MovimientoRequestDTO dto = buildRequest("478758", "Retiro", 540.0);

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setCuenta(cuenta);
            return m;
        });

        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        assertThat(resultado.getSaldo()).isEqualTo(0.0);
    }

    @Test
    void debeLanzarExcepcionPorSaldoInsuficiente() {
        Cuenta cuenta = cuentaConSaldo("478758", 0.0);
        MovimientoRequestDTO dto = buildRequest("478758", "Retiro", 150.0);

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> movimientoService.registrar(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Saldo no disponible");
    }

    @Test
    void debeLanzarExcepcionSiCuentaNoExiste() {
        when(cuentaRepository.findByNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movimientoService.registrar(buildRequest("000000", "Deposito", 100.0)))
                .isInstanceOf(RuntimeException.class);
    }

    private Cuenta cuentaConSaldo(String numeroCuenta, double saldoDisponible) {
        Cuenta c = new Cuenta();
        c.setId(1L);
        c.setNumeroCuenta(numeroCuenta);
        c.setTipoCuenta("Ahorro");
        c.setSaldoInicial(saldoDisponible);
        c.setSaldoDisponible(saldoDisponible);
        c.setEstado(true);
        c.setClienteId("CLI001");
        return c;
    }

    private MovimientoRequestDTO buildRequest(String numeroCuenta, String tipo, double valor) {
        return MovimientoRequestDTO.builder()
                .numeroCuenta(numeroCuenta)
                .tipoMovimiento(tipo)
                .valor(valor)
                .build();
    }
}
