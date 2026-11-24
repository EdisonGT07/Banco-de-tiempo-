package com.bancotiempo.pasaportesaberes.application.port.out;

public interface GeneradorToken {
    String generarTokenSesion();

    String generarCodigoCorto();

    String generarTokenAcceso(Long usuarioId);
}
