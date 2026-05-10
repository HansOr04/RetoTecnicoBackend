package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.exception.CuentaInactivaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.exception.MovimientoNoEncontradoException;
import com.banco.mscuentas.domain.exception.SaldoInsuficienteException;
import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.model.Movimiento;
import com.banco.mscuentas.domain.model.TipoMovimiento;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.domain.repository.MovimientoRepository;
import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import com.banco.mscuentas.dto.ReporteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MovimientoService implements IMovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public MovimientoService(MovimientoRepository movimientoRepository, CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarTodos() {
        return movimientoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public MovimientoResponseDTO registrar(MovimientoRequestDTO dto) {
        log.info("Registrando movimiento: cuenta={}, tipo={}, monto={}",
                dto.getNumeroCuenta(), dto.getTipoMovimiento(), dto.getValor());

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(dto.getNumeroCuenta()));

        if (Boolean.FALSE.equals(cuenta.getEstado())) {
            log.warn("Intento de operar en cuenta inactiva: {}", cuenta.getNumeroCuenta());
            throw new CuentaInactivaException(dto.getNumeroCuenta());
        }

        TipoMovimiento tipo = TipoMovimiento.fromString(dto.getTipoMovimiento());

        BigDecimal nuevoSaldo = switch (tipo) {
            case DEPOSITO -> cuenta.getSaldoDisponible().add(dto.getValor());
            case RETIRO -> {
                BigDecimal resultado = cuenta.getSaldoDisponible().subtract(dto.getValor());
                if (resultado.compareTo(BigDecimal.ZERO) < 0) {
                    log.warn("Saldo insuficiente: cuenta={}, saldoDisponible={}, montoSolicitado={}",
                            cuenta.getNumeroCuenta(), cuenta.getSaldoDisponible(), dto.getValor());
                    throw new SaldoInsuficienteException();
                }
                yield resultado;
            }
        };

        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipo.getDescripcion())
                .valor(dto.getValor())
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        MovimientoResponseDTO response = mapToResponse(movimientoRepository.save(movimiento));
        log.info("Movimiento registrado exitosamente: cuenta={}, nuevoSaldo={}",
                dto.getNumeroCuenta(), nuevoSaldo);
        return response;
    }

    @Override
    public MovimientoResponseDTO actualizar(Long id, MovimientoRequestDTO dto) {
        throw new UnsupportedOperationException(
                "Los movimientos son registros históricos inmutables y no pueden modificarse");
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!movimientoRepository.existsById(id)) {
            throw new MovimientoNoEncontradoException(id);
        }
        movimientoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDTO> generarReporte(String clienteId, LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository
                .findByCuenta_ClienteIdAndFechaBetween(clienteId, inicio, fin)
                .stream()
                .map(this::mapToReporte)
                .toList();
    }

    private MovimientoResponseDTO mapToResponse(Movimiento movimiento) {
        return MovimientoResponseDTO.builder()
                .id(movimiento.getId())
                .fecha(movimiento.getFecha())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .valor(movimiento.getValor())
                .saldo(movimiento.getSaldo())
                .numeroCuenta(movimiento.getCuenta().getNumeroCuenta())
                .build();
    }

    private ReporteDTO mapToReporte(Movimiento movimiento) {
        Cuenta cuenta = movimiento.getCuenta();
        TipoMovimiento tipo = TipoMovimiento.fromString(movimiento.getTipoMovimiento());
        BigDecimal valorConSigno = tipo == TipoMovimiento.RETIRO
                ? movimiento.getValor().negate()
                : movimiento.getValor();

        return ReporteDTO.builder()
                .fecha(movimiento.getFecha())
                .cliente(cuenta.getClienteId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .estado(cuenta.getEstado())
                .movimiento(valorConSigno)
                .saldoDisponible(movimiento.getSaldo())
                .build();
    }
}
