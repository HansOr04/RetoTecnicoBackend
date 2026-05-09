package com.banco.msclientes.domain.exception;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(String clienteId) {
        super("Cliente no encontrado: " + clienteId);
    }
}
