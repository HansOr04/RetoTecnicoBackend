package com.banco.mscuentas.domain.exception;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException() {
        super("Saldo no disponible");
    }
}
