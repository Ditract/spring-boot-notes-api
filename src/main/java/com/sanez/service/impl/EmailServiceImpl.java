package com.sanez.service.impl;

import com.sanez.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarEmailVerificacion(String destinatario, String token) {
        String asunto = "Verifica tu cuenta - Notas App";
        String linkVerificacion = baseUrl + "/api/auth/verify?token=" + token;

        String mensaje = "¡Bienvenido a Notas App!\n\n" +
                "Por favor, verifica tu cuenta haciendo clic en el siguiente enlace:\n\n" +
                linkVerificacion + "\n\n" +
                "Este enlace expirará en 24 horas.\n\n" +
                "Si no creaste esta cuenta, ignora este mensaje.\n\n" +
                "Saludos,\n" +
                "El equipo de Notas App";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(destinatario);
        email.setSubject(asunto);
        email.setText(mensaje);
        email.setFrom("noreply@notasapp.com");

        mailSender.send(email);
    }

    @Override
    public void enviarEmailRecuperacionPassword(String destinatario, String token) {
        String asunto = "Recuperación de contraseña - Notas App";
        String linkReset = baseUrl + "/api/auth/reset-password?token=" + token;

        String mensaje = "Hola,\n\n" +
                "Recibimos una solicitud para restablecer tu contraseña en Notas App.\n\n" +
                "Para crear una nueva contraseña, haz clic en el siguiente enlace:\n\n" +
                linkReset + "\n\n" +
                "Este enlace expirará en 1 hora.\n\n" +
                "Si no solicitaste este cambio, ignora este mensaje. Tu contraseña permanecerá sin cambios.\n\n" +
                "Saludos,\n" +
                "El equipo de Notas App";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(destinatario);
        email.setSubject(asunto);
        email.setText(mensaje);
        email.setFrom("noreply@notasapp.com");

        mailSender.send(email);
    }
}
