package com.sanez.service.impl;

import com.sanez.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Esta implementación usa Mailtrap como servicio de testing de emails.
 * Solo se activa cuando el perfil 'dev' está activo.
 */
@Service
@Profile("dev")
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("EmailServiceImpl inicializado para desarrollo (Mailtrap)");
    }

    @Override
    public void enviarEmailVerificacion(String destinatario, String token) {
        log.info("Enviando email de verificación a: {} (via Mailtrap)", destinatario);

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

        try {
            mailSender.send(email);
            log.info("Email de verificación enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error al enviar email de verificación", e);
        }
    }

    @Override
    public void enviarEmailRecuperacionPassword(String destinatario, String token) {
        log.info("Enviando email de recuperación de contraseña a: {} (via Mailtrap)", destinatario);

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

        try {
            mailSender.send(email);
            log.info("Email de recuperación enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error al enviar email de recuperación", e);
        }
    }
}