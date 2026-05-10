package com.banco.mscuentas.domain.exception;

public class CuentaConMovimientosException extends RuntimeException {

    public CuentaConMovimientosException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " tiene movimientos registrados y no puede eliminarse");
    }
}
