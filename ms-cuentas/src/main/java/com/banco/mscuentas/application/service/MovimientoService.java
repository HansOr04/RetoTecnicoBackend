package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.model.Movimiento;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.domain.repository.MovimientoRepository;
import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import com.banco.mscuentas.dto.ReporteDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public MovimientoService(MovimientoRepository movimientoRepository, CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public MovimientoResponseDTO registrar(MovimientoRequestDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        double nuevoSaldo;
        if ("Deposito".equalsIgnoreCase(dto.getTipoMovimiento())) {
            nuevoSaldo = cuenta.getSaldoDisponible() + dto.getValor();
        } else if ("Retiro".equalsIgnoreCase(dto.getTipoMovimiento())) {
            nuevoSaldo = cuenta.getSaldoDisponible() - dto.getValor();
        } else {
            throw new RuntimeException("Tipo de movimiento no válido. Use 'Deposito' o 'Retiro'");
        }

        if (nuevoSaldo < 0) {
            throw new RuntimeException("Saldo no disponible");
        }

        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(dto.getTipoMovimiento())
                .valor(dto.getValor())
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        return mapToResponse(movimientoRepository.save(movimiento));
    }

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
        double valorConSigno = "Retiro".equalsIgnoreCase(movimiento.getTipoMovimiento())
                ? -movimiento.getValor()
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
