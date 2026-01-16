package com.sanez.exception;

public class PasswordIncorrectaException extends RuntimeException {
    public PasswordIncorrectaException(String mensaje) {
        super(mensaje);
    }
}