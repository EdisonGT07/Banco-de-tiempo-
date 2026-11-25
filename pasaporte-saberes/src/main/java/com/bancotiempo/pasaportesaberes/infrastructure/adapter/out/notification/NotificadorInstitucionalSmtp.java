package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.notification;

import com.bancotiempo.pasaportesaberes.application.port.out.NotificadorInstitucional;
import org.springframework.stereotype.Component;

@Component
public class NotificadorInstitucionalSmtp implements NotificadorInstitucional {
    private final CorreoSmtp correo;

    public NotificadorInstitucionalSmtp(CorreoSmtp correo) {
        this.correo = correo;
    }

    @Override
    public void notificarSolicitudAval(
            String correoEntidad,
            String nombrePersona,
            String codigoCredencial
    ) {
        if (nombrePersona == null || nombrePersona.isBlank()
                || codigoCredencial == null || codigoCredencial.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la persona y el código de credencial son obligatorios");
        }
        correo.enviar(
                correoEntidad,
                "Solicitud de aval de credencial",
                "La persona " + nombrePersona + " solicita la revisión de la credencial "
                        + codigoCredencial + "."
        );
    }
}
