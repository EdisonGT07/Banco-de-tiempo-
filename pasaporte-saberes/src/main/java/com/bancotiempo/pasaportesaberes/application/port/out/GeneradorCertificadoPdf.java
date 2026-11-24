package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;

public interface GeneradorCertificadoPdf {
    byte[] generar(PasaporteUseCases.CredencialPublica credencial, String codigoVerificacion);
}
