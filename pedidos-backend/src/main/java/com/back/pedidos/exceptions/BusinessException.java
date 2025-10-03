package com.back.pedidos.exceptions;

public class BusinessException extends PedidoProcessingException {
    public BusinessException(String message) {
        super(message);
    }
}