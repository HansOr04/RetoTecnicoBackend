package com.banco.mscuentas.application.service;

import com.banco.mscuentas.dto.MovimientoRequestDTO;
import com.banco.mscuentas.dto.MovimientoResponseDTO;
import com.banco.mscuentas.dto.ReporteDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de casos de uso para movimientos y reportes bancarios.
 *
 * <p>Reglas de negocio garantizadas por las implementaciones:
 * <ol>
 *   <li>Depósito incrementa el saldoDisponible de la cuenta.</li>
 *   <li>Retiro decrementa el saldoDisponible de la cuenta.</li>
 *   <li>Si el saldo resultante sería negativo, se lanza {@link com.banco.mscuentas.domain.exception.SaldoInsuficienteException}.</li>
 *   <li>El valor del movimiento siempre se almacena positivo; el tipo determina su efecto.</li>
 *   <li>La fecha es asignada por el servidor (LocalDateTime.now()), nunca por el cliente.</li>
 *   <li>En el reporte, el campo {@code movimiento} lleva signo: negativo para retiros.</li>
 *   <li>Los movimientos son registros históricos inmutables en producción.</li>
 * </ol>
 */
public interface IMovimientoService {

    /**
     * Retorna todos los movimientos registrados.
     *
     * @return lista de movimientos
     */
    List<MovimientoResponseDTO> listarTodos();

    /**
     * Registra un movimiento (depósito o retiro) y actualiza el saldo disponible.
     *
     * @param dto datos del movimiento
     * @return DTO del movimiento registrado con saldo resultante
     * @throws com.banco.mscuentas.domain.exception.CuentaNoEncontradaException si la cuenta no existe
     * @throws com.banco.mscuentas.domain.exception.SaldoInsuficienteException  si el retiro excede el saldo
     * @throws IllegalArgumentException                                          si el tipo de movimiento no es válido
     */
    MovimientoResponseDTO registrar(MovimientoRequestDTO dto);

    /**
     * Actualiza los datos de un movimiento existente.
     * En producción este endpoint debe estar protegido por autorización.
     *
     * @param id  ID del movimiento
     * @param dto nuevos datos
     * @return DTO actualizado
     */
    MovimientoResponseDTO actualizar(Long id, MovimientoRequestDTO dto);

    /**
     * Elimina un movimiento por ID.
     * En producción este endpoint debe estar protegido por autorización.
     *
     * @param id ID del movimiento a eliminar
     */
    void eliminar(Long id);

    /**
     * Genera el reporte de estado de cuenta filtrando por cliente y rango de fechas.
     *
     * @param clienteId referencia lógica al cliente
     * @param inicio    fecha y hora de inicio del rango (inclusive)
     * @param fin       fecha y hora de fin del rango (inclusive)
     * @return lista de movimientos con datos de cuenta asociada y valor con signo
     */
    List<ReporteDTO> generarReporte(String clienteId, LocalDateTime inicio, LocalDateTime fin);
}
