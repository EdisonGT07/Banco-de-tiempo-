package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Credencial;

public interface VerificadorCredenciales {
    boolean verificar(Credencial credencial);
}
