package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

public interface RepositorioTransaccion {
    Optional<Transaccion> buscarPorClaveIdempotencia(String clave);

    BigDecimal sumarRetiros(Long personaId, YearMonth periodo);

    Transaccion guardar(Transaccion transaccion);
}
