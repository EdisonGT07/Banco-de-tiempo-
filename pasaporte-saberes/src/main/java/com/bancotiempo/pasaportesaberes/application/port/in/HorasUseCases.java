package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;

import java.math.BigDecimal;

public interface HorasUseCases {
    Billetera consultarBilletera(Long personaId);

    Transaccion retirarDelFondo(RetiroFondo solicitud);

    record RetiroFondo(
            Long personaId,
            BigDecimal horas,
            String claveIdempotencia
    ) {
    }
}
