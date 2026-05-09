package com.banco.mscuentas.domain.exception;

public class CuentaDuplicadaException extends RuntimeException {

    public CuentaDuplicadaException(String numeroCuenta) {
        super("Ya existe una cuenta con número: " + numeroCuenta);
    }
}
