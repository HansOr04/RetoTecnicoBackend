package com.banco.msclientes.domain.exception;

public class ClienteDuplicadoException extends RuntimeException {

    public ClienteDuplicadoException(String clienteId) {
        super("Ya existe un cliente con el id: " + clienteId);
    }
}
