package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.TransaccionInvalidaException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
        validarDatosObligatorios(tipo, horas, billeteraOrigen, billeteraDestino);
        validarBilleteras(tipo, billeteraOrigen, billeteraDestino);
        validarSesion(tipo, sesion);
        if (claveIdempotencia == null || claveIdempotencia.isBlank()) {
            throw new TransaccionInvalidaException("La clave de idempotencia es obligatoria");
        }
        if (creadaEn == null) {
            throw new TransaccionInvalidaException("La fecha de creación es obligatoria");
        }
    }

    private static void validarDatosObligatorios(
            TipoTransaccion tipo,
            BigDecimal horas,
            Billetera billeteraOrigen,
            Billetera billeteraDestino
    ) {
        if (tipo == null) {
            throw new TransaccionInvalidaException("El tipo de transacción es obligatorio");
        }
        if (horas == null || horas.signum() <= 0) {
            throw new TransaccionInvalidaException("Las horas deben ser mayores que cero");
        }
        if (billeteraOrigen == null) {
            throw new TransaccionInvalidaException("La billetera origen es obligatoria");
        }
        if (billeteraDestino == null) {
            throw new TransaccionInvalidaException("La billetera destino es obligatoria");
        }
    }

    private static void validarBilleteras(
            TipoTransaccion tipo,
            Billetera billeteraOrigen,
            Billetera billeteraDestino
    ) {
        Billetera.TipoBilletera origen = billeteraOrigen.getTipo();
        Billetera.TipoBilletera destino = billeteraDestino.getTipo();

        switch (tipo) {
            case BONO -> exigir(origen == Billetera.TipoBilletera.EMISOR
                            && destino == Billetera.TipoBilletera.USUARIO,
                    "Un BONO debe moverse desde EMISOR hacia una billetera USUARIO");
            case RESERVA, LIBERACION -> exigir(
                    mismaBilletera(billeteraOrigen, billeteraDestino)
                            && origen == Billetera.TipoBilletera.USUARIO,
                    tipo + " debe mover horas entre los saldos de la misma billetera USUARIO");
            case GANADA -> exigir(
                    origen == Billetera.TipoBilletera.USUARIO
                            && destino == Billetera.TipoBilletera.USUARIO
                            && !mismaBilletera(billeteraOrigen, billeteraDestino),
                    "GANADA debe mover horas entre las billeteras de dos usuarios distintos");
            case DONADA -> exigir(
                    origen == Billetera.TipoBilletera.USUARIO
                            && (destino == Billetera.TipoBilletera.FONDO
                            || destino == Billetera.TipoBilletera.USUARIO)
                            && !mismaBilletera(billeteraOrigen, billeteraDestino),
                    "DONADA debe mover horas desde un usuario al FONDO u otro usuario");
            case EXCEDENTE -> exigir(
                    origen == Billetera.TipoBilletera.USUARIO
                            && destino == Billetera.TipoBilletera.FONDO,
                    "EXCEDENTE debe moverse desde una billetera USUARIO al FONDO");
            case RETIRO_FONDO -> exigir(
                    origen == Billetera.TipoBilletera.FONDO
                            && destino == Billetera.TipoBilletera.USUARIO,
                    "RETIRO_FONDO debe moverse desde FONDO hacia una billetera USUARIO");
        }
    }

    private static void validarSesion(TipoTransaccion tipo, Sesion sesion) {
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
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new TransaccionInvalidaException(mensaje);
        }
    }

    private static boolean mismaBilletera(Billetera primera, Billetera segunda) {
        if (primera == segunda) {
            return true;
        }
        Long primerId = primera.getId();
        Long segundoId = segunda.getId();
        return primerId != null && primerId.equals(segundoId);
    }
}
