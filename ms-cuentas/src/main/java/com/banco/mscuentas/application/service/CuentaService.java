package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.exception.CuentaConMovimientosException;
import com.banco.mscuentas.domain.exception.CuentaDuplicadaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.domain.repository.MovimientoRepository;
import com.banco.mscuentas.dto.CuentaRequestDTO;
import com.banco.mscuentas.dto.CuentaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link ICuentaService}.
 * Al crear una cuenta, saldoDisponible se inicializa igual al saldoInicial.
 * La operación actualizar no modifica saldoDisponible; ese campo lo gestiona MovimientoService.
 */
@Service
public class CuentaService implements ICuentaService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    public CuentaService(CuentaRepository cuentaRepository, MovimientoRepository movimientoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> listarTodas() {
        return cuentaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return mapToResponse(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> obtenerPorClienteId(String clienteId) {
        return cuentaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public CuentaResponseDTO crear(CuentaRequestDTO dto) {
        if (cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta()).isPresent()) {
            throw new CuentaDuplicadaException(dto.getNumeroCuenta());
        }
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(dto.getNumeroCuenta())
                .tipoCuenta(dto.getTipoCuenta())
                .saldoInicial(dto.getSaldoInicial())
                .saldoDisponible(dto.getSaldoInicial())
                .estado(dto.getEstado())
                .clienteId(dto.getClienteId())
                .build();
        return mapToResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public CuentaResponseDTO actualizar(String numeroCuenta, CuentaRequestDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        cuenta.setEstado(dto.getEstado());
        cuenta.setClienteId(dto.getClienteId());
        return mapToResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public void eliminar(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        if (movimientoRepository.existsByCuenta(cuenta)) {
            throw new CuentaConMovimientosException(numeroCuenta);
        }
        cuentaRepository.deleteById(cuenta.getId());
    }

    private CuentaResponseDTO mapToResponse(Cuenta cuenta) {
        return CuentaResponseDTO.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getClienteId())
                .build();
    }
}
