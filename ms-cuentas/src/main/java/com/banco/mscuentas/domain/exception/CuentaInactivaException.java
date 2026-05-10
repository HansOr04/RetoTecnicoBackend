package com.banco.mscuentas.domain.exception;

public class CuentaInactivaException extends RuntimeException {
    public CuentaInactivaException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " se encuentra inactiva");
    }
}
