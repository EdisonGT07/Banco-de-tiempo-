package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.TransaccionInvalidaException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record Transaccion(
    Long id,
    TipoTransaccion tipo,
    BigDecimal horas,
    Billetera billeteraOrigen,
    Billetera billeteraDestino,
    Sesion sesion,
    String claveIdempotencia,
    OffsetDateTime creadaEn
) {
    public enum TipoTransaccion {
        BONO,
        RESERVA,
        LIBERACION,
        GANADA,
        DONADA,
        EXCEDENTE,
        RETIRO_FONDO
    }

    public Transaccion {
        Objects.requireNonNull(tipo, "El tipo de transacción es obligatorio");
        if (horas == null || horas.signum() <= 0) {
            throw new TransaccionInvalidaException("Las horas deben ser mayores que cero");
        }
        Objects.requireNonNull(billeteraOrigen, "La billetera origen es obligatoria");
        Objects.requireNonNull(billeteraDestino, "La billetera destino es obligatoria");

        boolean reservaOLiberacion =
                tipo == TipoTransaccion.RESERVA || tipo == TipoTransaccion.LIBERACION;

        if (!reservaOLiberacion
                && billeteraOrigen.getId() != null
                && billeteraOrigen.getId().equals(billeteraDestino.getId())) {
            throw new TransaccionInvalidaException(
                    "El origen y el destino deben ser diferentes para " + tipo);
        }

        boolean exigeSesion =
                tipo == TipoTransaccion.RESERVA
                        || tipo == TipoTransaccion.LIBERACION
                        || tipo == TipoTransaccion.GANADA
                        || tipo == TipoTransaccion.DONADA
                        || tipo == TipoTransaccion.EXCEDENTE;
        if (exigeSesion && sesion == null) {
            throw new TransaccionInvalidaException(
                    "El tipo " + tipo + " debe estar asociado a una sesión");
        }
        if (claveIdempotencia == null || claveIdempotencia.isBlank()) {
            throw new TransaccionInvalidaException("La clave de idempotencia es obligatoria");
        }
    }
}
