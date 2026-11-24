package com.bancotiempo.pasaportesaberes.application.port.out;

public interface AlmacenEvidencias {
    String guardar(Long sesionId, byte[] contenido, String tipoContenido);
}
