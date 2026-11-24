package com.bancotiempo.pasaportesaberes.application.port.out;

public interface NotificadorInstitucional {
    void notificarSolicitudAval(
            String correoEntidad,
            String nombrePersona,
            String codigoCredencial
    );
}
