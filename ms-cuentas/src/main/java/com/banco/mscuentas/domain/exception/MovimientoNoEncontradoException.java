package com.banco.mscuentas.domain.exception;

public class MovimientoNoEncontradoException extends RuntimeException {
    public MovimientoNoEncontradoException(Long id) {
        super("Movimiento no encontrado: " + id);
    }
}
