package com.bancotiempo.pasaportesaberes.application.port.out;

public interface GeneradorHmacDocumento {
    String generar(String documento);
}
