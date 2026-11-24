package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.TransaccionInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        horas = normalizarHoras(horas);
        validarBilleteras(tipo, billeteraOrigen, billeteraDestino);
        validarSesion(tipo, sesion);
        validarParticipantesSesion(tipo, billeteraOrigen, billeteraDestino, sesion);
        validarClaveIdempotencia(
                tipo, billeteraOrigen, billeteraDestino, sesion, claveIdempotencia);
        if (creadaEn == null) {
            throw new TransaccionInvalidaException("La fecha de creación es obligatoria");
        }
    }

    public static String claveBono(Long usuarioId) {
        return claveConId("bono", usuarioId);
    }

    public static String claveReserva(Long sesionId) {
        return claveConId("reserva", sesionId);
    }

    public static String claveLiberacion(Long sesionId) {
        return claveConId("liberacion", sesionId);
    }

    public static String claveLiquidacion(Long sesionId) {
        return claveConId("liquidacion", sesionId);
    }

    public static String claveExcedente(Long sesionId, Long billeteraId) {
        validarIdPositivo(sesionId, "La sesión");
        validarIdPositivo(billeteraId, "La billetera");
        return "excedente-" + sesionId + "-" + billeteraId;
    }

    public void aplicar() {
        switch (tipo) {
            case RESERVA -> billeteraOrigen.reservar(horas);
            case LIBERACION -> billeteraOrigen.liberar(horas);
            case GANADA, DONADA ->
                    billeteraOrigen.transferirReservadoA(billeteraDestino, horas);
            case BONO, EXCEDENTE, RETIRO_FONDO ->
                    billeteraOrigen.transferirDisponibleA(billeteraDestino, horas);
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

    private static BigDecimal normalizarHoras(BigDecimal horas) {
        BigDecimal normalizadas;
        try {
            normalizadas = horas.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new TransaccionInvalidaException(
                    "Las horas deben tener como máximo dos decimales");
        }
        if (normalizadas.precision() > 8) {
            throw new TransaccionInvalidaException("Las horas exceden la precisión NUMERIC(8,2)");
        }
        return normalizadas;
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

    private static void validarClaveIdempotencia(
            TipoTransaccion tipo,
            Billetera origen,
            Billetera destino,
            Sesion sesion,
            String clave
    ) {
        if (clave == null || clave.isBlank() || clave.length() > 80) {
            throw new TransaccionInvalidaException(
                    "La clave de idempotencia es obligatoria y no puede superar 80 caracteres");
        }
        String esperada = switch (tipo) {
            case BONO -> claveBono(idPersona(destino));
            case RESERVA -> claveReserva(idSesion(sesion));
            case LIBERACION -> claveLiberacion(idSesion(sesion));
            case GANADA, DONADA -> claveLiquidacion(idSesion(sesion));
            case EXCEDENTE -> claveExcedente(idSesion(sesion), origen.getId());
            case RETIRO_FONDO -> null;
        };
        if (esperada != null && !esperada.equals(clave)) {
            throw new TransaccionInvalidaException(
                    "La clave de idempotencia no corresponde al tipo y entidad de la transacción");
        }
    }

    private static String claveConId(String prefijo, Long id) {
        validarIdPositivo(id, "El identificador");
        return prefijo + "-" + id;
    }

    private static void validarIdPositivo(Long id, String nombre) {
        if (id == null || id <= 0) {
            throw new TransaccionInvalidaException(nombre + " debe tener un id positivo");
        }
    }

    private static Long idPersona(Billetera billetera) {
        return billetera.getPersona() == null ? null : billetera.getPersona().getId();
    }

    private static Long idSesion(Sesion sesion) {
        return sesion == null ? null : sesion.id();
    }

    private static void validarParticipantesSesion(
            TipoTransaccion tipo,
            Billetera origen,
            Billetera destino,
            Sesion sesion
    ) {
        if (sesion == null) {
            return;
        }

        Oferta oferta = sesion.oferta();
        Persona aprendiz = sesion.aprendiz();
        Persona tutor = oferta.tutor();
        if (tipo == TipoTransaccion.RESERVA || tipo == TipoTransaccion.LIBERACION
                || tipo == TipoTransaccion.GANADA || tipo == TipoTransaccion.DONADA) {
            exigir(mismaPersona(origen.getPersona(), aprendiz),
                    tipo + " debe usar la billetera del aprendiz de la sesión");
        }

        switch (tipo) {
            case GANADA -> exigir(
                    oferta.destinoHoras() == Oferta.DestinoHoras.BILLETERA_PROPIA
                            && mismaPersona(destino.getPersona(), tutor),
                    "GANADA debe acreditar al tutor cuando el destino es BILLETERA_PROPIA");
            case DONADA -> {
                if (oferta.destinoHoras() == Oferta.DestinoHoras.FONDO) {
                    exigir(destino.getTipo() == Billetera.TipoBilletera.FONDO,
                            "DONADA debe ir al FONDO según el destino de la oferta");
                } else if (oferta.destinoHoras() == Oferta.DestinoHoras.REGALO) {
                    exigir(mismaPersona(destino.getPersona(), oferta.receptorRegalo()),
                            "DONADA debe acreditar al receptor definido en la oferta");
                } else {
                    throw new TransaccionInvalidaException(
                            "Una oferta BILLETERA_PROPIA debe liquidarse como GANADA");
                }
            }
            case EXCEDENTE -> exigir(mismaPersona(origen.getPersona(), tutor),
                    "EXCEDENTE debe salir de la billetera del tutor de la sesión");
            default -> {
            }
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

    private static boolean mismaPersona(Persona primera, Persona segunda) {
        if (primera == null || segunda == null) {
            return false;
        }
        if (primera == segunda) {
            return true;
        }
        return primera.getId() != null && primera.getId().equals(segunda.getId());
    }
}
