package com.sanez.exception;

public class EnvioEmailException extends RuntimeException {

    public EnvioEmailException(String mensaje) {
        super(mensaje);
    }

    public EnvioEmailException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}