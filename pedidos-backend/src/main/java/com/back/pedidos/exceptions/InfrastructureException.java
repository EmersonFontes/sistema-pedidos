package com.back.pedidos.exceptions;

public class InfrastructureException extends PedidoProcessingException {
    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}