package com.banco.mscuentas.application.service;

import com.banco.mscuentas.dto.CuentaRequestDTO;
import com.banco.mscuentas.dto.CuentaResponseDTO;

import java.util.List;

/**
 * Contrato de casos de uso para la gestión de cuentas bancarias.
 * Al crear una cuenta, saldoDisponible se inicializa igual a saldoInicial.
 */
public interface ICuentaService {

    /**
     * Retorna todas las cuentas registradas.
     *
     * @return lista de cuentas; vacía si no hay registros
     */
    List<CuentaResponseDTO> listarTodas();

    /**
     * Busca una cuenta por número de cuenta.
     *
     * @param numeroCuenta número de cuenta único
     * @return DTO de la cuenta encontrada
     * @throws com.banco.mscuentas.domain.exception.CuentaNoEncontradaException si no existe
     */
    CuentaResponseDTO obtenerPorNumeroCuenta(String numeroCuenta);

    /**
     * Retorna todas las cuentas asociadas a un clienteId.
     *
     * @param clienteId referencia lógica al cliente en ms-clientes
     * @return lista de cuentas del cliente
     */
    List<CuentaResponseDTO> obtenerPorClienteId(String clienteId);

    /**
     * Crea una cuenta nueva. El saldoDisponible inicial es igual al saldoInicial.
     *
     * @param dto datos de la cuenta
     * @return DTO de la cuenta creada
     * @throws com.banco.mscuentas.domain.exception.CuentaDuplicadaException si el número ya existe
     */
    CuentaResponseDTO crear(CuentaRequestDTO dto);

    /**
     * Actualiza los datos de una cuenta existente. No modifica saldoDisponible.
     *
     * @param numeroCuenta número de cuenta a actualizar
     * @param dto          nuevos datos
     * @return DTO actualizado
     * @throws com.banco.mscuentas.domain.exception.CuentaNoEncontradaException si no existe
     */
    CuentaResponseDTO actualizar(String numeroCuenta, CuentaRequestDTO dto);

    /**
     * Elimina una cuenta por número.
     *
     * @param numeroCuenta número de cuenta a eliminar
     * @throws com.banco.mscuentas.domain.exception.CuentaNoEncontradaException si no existe
     */
    void eliminar(String numeroCuenta);
}
