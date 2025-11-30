package com.sanez.service;

public interface EmailService {

    void enviarEmailVerificacion(String destinatario, String token);
    void enviarEmailRecuperacionPassword(String destinatario, String token);
}
