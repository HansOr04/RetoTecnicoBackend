package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.exception.CuentaDuplicadaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.dto.CuentaRequestDTO;
import com.banco.mscuentas.dto.CuentaResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de {@link ICuentaService}.
 * Al crear una cuenta, saldoDisponible se inicializa igual al saldoInicial.
 * La operación actualizar no modifica saldoDisponible; ese campo lo gestiona MovimientoService.
 */
@Service
public class CuentaService implements ICuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public List<CuentaResponseDTO> listarTodas() {
        return cuentaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CuentaResponseDTO obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return mapToResponse(cuenta);
    }

    @Override
    public List<CuentaResponseDTO> obtenerPorClienteId(String clienteId) {
        return cuentaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
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
    public void eliminar(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
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
