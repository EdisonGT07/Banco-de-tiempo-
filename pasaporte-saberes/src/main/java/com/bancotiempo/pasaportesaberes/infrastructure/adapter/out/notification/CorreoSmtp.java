package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class CorreoSmtp {
    private final JavaMailSender mailSender;
    private final String host;
    private final String remitente;

    public CorreoSmtp(
            JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String host,
            @Value("${pasaporte-saberes.notificaciones.from:}") String remitente
    ) {
        this.mailSender = mailSender;
        this.host = host;
        this.remitente = remitente;
    }

    public void enviar(String destinatario, String asunto, String contenido) {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Falta configurar SMTP_HOST");
        }
        if (remitente == null || remitente.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar PASAPORTE_NOTIFICACIONES_FROM");
        }
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("El destinatario del correo es obligatorio");
        }
        if (asunto == null || asunto.isBlank() || contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El asunto y contenido del correo son obligatorios");
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(contenido);
        mailSender.send(mensaje);
    }
}
