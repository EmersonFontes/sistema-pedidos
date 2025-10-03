package com.back.pedidos.exceptions;

public abstract class PedidoProcessingException extends Exception {

    public PedidoProcessingException(String message) {
        super(message);
    }

    public PedidoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
