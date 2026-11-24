package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.SesionInvalidaException;

import java.time.Duration;
import java.time.OffsetDateTime;

public record Sesion(
    Long id,
    Oferta oferta,
    Persona aprendiz,
    OffsetDateTime iniciaEn,
    String lugarOEnlace,
    EstadoSesion estado,
    OffsetDateTime finConfirmacionTutor,
    OffsetDateTime finConfirmacionAprendiz,
    Inasistente inasistente,
    long version,
    OffsetDateTime creadaEn
) {
    public enum EstadoSesion {
        PROGRAMADA,
        RESERVADA,
        EN_CURSO,
        PENDIENTE_CONFIRMACION,
        COMPLETADA,
        CANCELADA,
        NO_ASISTIO,
        EXPIRADA,
        DISPUTADA
    }

    public enum Inasistente {
        TUTOR,
        APRENDIZ
    }

    public Sesion {
        if (oferta == null || oferta.tutor() == null) {
            throw new SesionInvalidaException("La sesión debe tener una oferta con tutor");
        }
        if (iniciaEn == null || creadaEn == null || estado == null) {
            throw new SesionInvalidaException(
                    "La fecha de inicio, creación y el estado son obligatorios");
        }
        if (aprendiz != null && mismaPersona(aprendiz, oferta.tutor())) {
            throw new SesionInvalidaException("El tutor no puede ser el aprendiz de su sesión");
        }
        if (estado == EstadoSesion.PROGRAMADA && aprendiz != null) {
            throw new SesionInvalidaException("Una sesión PROGRAMADA aún no tiene aprendiz");
        }
        if (requiereAprendiz(estado) && aprendiz == null) {
            throw new SesionInvalidaException(
                    "El estado " + estado + " requiere un aprendiz");
        }
        if (estado == EstadoSesion.PENDIENTE_CONFIRMACION
                && ((finConfirmacionTutor == null) == (finConfirmacionAprendiz == null))) {
            throw new SesionInvalidaException(
                    "PENDIENTE_CONFIRMACION requiere exactamente una confirmación");
        }
        if (estado == EstadoSesion.COMPLETADA
                && (finConfirmacionTutor == null || finConfirmacionAprendiz == null)) {
            throw new SesionInvalidaException(
                    "COMPLETADA requiere las confirmaciones del tutor y del aprendiz");
        }
        if (estado == EstadoSesion.NO_ASISTIO && inasistente == null) {
            throw new SesionInvalidaException("NO_ASISTIO requiere identificar al inasistente");
        }
        if (estado != EstadoSesion.NO_ASISTIO && inasistente != null) {
            throw new SesionInvalidaException(
                    "Solo una sesión NO_ASISTIO puede tener un inasistente");
        }
        if ((estado == EstadoSesion.PROGRAMADA || estado == EstadoSesion.RESERVADA
                || estado == EstadoSesion.EN_CURSO || estado == EstadoSesion.CANCELADA
                || estado == EstadoSesion.NO_ASISTIO)
                && (finConfirmacionTutor != null || finConfirmacionAprendiz != null)) {
            throw new SesionInvalidaException(
                    "El estado " + estado + " no puede tener confirmaciones de fin");
        }
        if (estado == EstadoSesion.EXPIRADA
                && ((finConfirmacionTutor == null) == (finConfirmacionAprendiz == null))) {
            throw new SesionInvalidaException(
                    "EXPIRADA requiere una sola confirmación de fin pendiente");
        }
        if (estado == EstadoSesion.DISPUTADA
                && finConfirmacionTutor != null && finConfirmacionAprendiz != null) {
            throw new SesionInvalidaException(
                    "Una sesión con ambas confirmaciones no puede estar DISPUTADA");
        }
    }

    public Sesion reservar(Persona nuevoAprendiz) {
        exigirEstado(EstadoSesion.PROGRAMADA);
        if (nuevoAprendiz == null) {
            throw new SesionInvalidaException("El aprendiz es obligatorio para reservar");
        }
        if (mismaPersona(nuevoAprendiz, oferta.tutor())) {
            throw new SesionInvalidaException("El tutor no puede reservar su propia sesión");
        }
        return nueva(
                nuevoAprendiz,
                EstadoSesion.RESERVADA,
                null,
                null,
                null
        );
    }

    public Sesion confirmarHandshake(Persona actor) {
        exigirEstado(EstadoSesion.RESERVADA);
        if (!mismaPersona(actor, aprendiz)) {
            throw new SesionInvalidaException(
                    "Solo el aprendiz puede confirmar el handshake de la sesión");
        }
        return nueva(
                aprendiz,
                EstadoSesion.EN_CURSO,
                null,
                null,
                null
        );
    }

    public Sesion confirmarFin(Persona actor, OffsetDateTime confirmadoEn) {
        if (confirmadoEn == null) {
            throw new SesionInvalidaException("La fecha de confirmación es obligatoria");
        }
        boolean esTutor = mismaPersona(actor, oferta.tutor());
        boolean esAprendiz = mismaPersona(actor, aprendiz);
        if (!esTutor && !esAprendiz) {
            throw new SesionInvalidaException(
                    "Solo el tutor o el aprendiz pueden confirmar el fin");
        }
        if (esTutor && finConfirmacionTutor != null
                || esAprendiz && finConfirmacionAprendiz != null) {
            return this;
        }
        if (estado != EstadoSesion.EN_CURSO
                && estado != EstadoSesion.PENDIENTE_CONFIRMACION) {
            throw new SesionInvalidaException(
                    "Solo una sesión EN_CURSO o PENDIENTE_CONFIRMACION puede confirmarse");
        }

        OffsetDateTime nuevaConfirmacionTutor =
                esTutor ? confirmadoEn : finConfirmacionTutor;
        OffsetDateTime nuevaConfirmacionAprendiz =
                esAprendiz ? confirmadoEn : finConfirmacionAprendiz;
        EstadoSesion nuevoEstado =
                nuevaConfirmacionTutor != null && nuevaConfirmacionAprendiz != null
                        ? EstadoSesion.COMPLETADA
                        : EstadoSesion.PENDIENTE_CONFIRMACION;
        return nueva(
                aprendiz,
                nuevoEstado,
                nuevaConfirmacionTutor,
                nuevaConfirmacionAprendiz,
                null
        );
    }

    public Sesion cancelar(Persona actor, OffsetDateTime ahora) {
        validarActorParticipante(actor);
        if (estado != EstadoSesion.PROGRAMADA && estado != EstadoSesion.RESERVADA) {
            throw new SesionInvalidaException(
                    "Solo una sesión PROGRAMADA o RESERVADA puede cancelarse");
        }
        if (ahora == null || !ahora.isBefore(iniciaEn)) {
            throw new SesionInvalidaException(
                    "La sesión solo puede cancelarse antes de la hora programada");
        }
        return nueva(aprendiz, EstadoSesion.CANCELADA, null, null, null);
    }

    public Sesion marcarInasistencia(
            Persona actor,
            Inasistente inasistente,
            OffsetDateTime ahora,
            Duration plazoHandshake
    ) {
        exigirEstado(EstadoSesion.RESERVADA);
        validarPlazo(plazoHandshake, "El plazo de handshake");
        if (ahora == null) {
            throw new SesionInvalidaException("La fecha de evaluación es obligatoria");
        }
        OffsetDateTime limiteHandshake = iniciaEn.plus(plazoHandshake);
        if (!ahora.isAfter(limiteHandshake)) {
            throw new SesionInvalidaException(
                    "El plazo de handshake aún no ha vencido");
        }
        validarActorParticipante(actor);
        if (inasistente == null) {
            throw new SesionInvalidaException("Debe identificarse al inasistente");
        }
        boolean actorEsTutor = mismaPersona(actor, oferta.tutor());
        if (actorEsTutor == (inasistente == Inasistente.TUTOR)) {
            throw new SesionInvalidaException(
                    "Quien reporta la inasistencia debe ser el participante que sí asistió");
        }
        return nueva(
                aprendiz,
                EstadoSesion.NO_ASISTIO,
                null,
                null,
                inasistente
        );
    }

    public Sesion expirar(OffsetDateTime ahora, Duration plazoConfirmacion) {
        exigirEstado(EstadoSesion.PENDIENTE_CONFIRMACION);
        validarPlazo(plazoConfirmacion, "El plazo de confirmación");
        if (ahora == null) {
            throw new SesionInvalidaException("La fecha de evaluación es obligatoria");
        }
        OffsetDateTime primeraConfirmacion = finConfirmacionTutor != null
                ? finConfirmacionTutor
                : finConfirmacionAprendiz;
        if (!ahora.isAfter(primeraConfirmacion.plus(plazoConfirmacion))) {
            throw new SesionInvalidaException(
                    "El plazo para confirmar el fin aún no ha vencido");
        }
        return nueva(
                aprendiz,
                EstadoSesion.EXPIRADA,
                finConfirmacionTutor,
                finConfirmacionAprendiz,
                null
        );
    }

    public Sesion disputar(Persona actor) {
        validarActorParticipante(actor);
        if (estado != EstadoSesion.RESERVADA && estado != EstadoSesion.EN_CURSO
                && estado != EstadoSesion.PENDIENTE_CONFIRMACION) {
            throw new SesionInvalidaException(
                    "Solo una sesión activa puede pasar a DISPUTADA");
        }
        return nueva(
                aprendiz,
                EstadoSesion.DISPUTADA,
                finConfirmacionTutor,
                finConfirmacionAprendiz,
                null
        );
    }

    private Sesion nueva(
            Persona nuevoAprendiz,
            EstadoSesion nuevoEstado,
            OffsetDateTime nuevaConfirmacionTutor,
            OffsetDateTime nuevaConfirmacionAprendiz,
            Inasistente nuevoInasistente
    ) {
        return new Sesion(
                id,
                oferta,
                nuevoAprendiz,
                iniciaEn,
                lugarOEnlace,
                nuevoEstado,
                nuevaConfirmacionTutor,
                nuevaConfirmacionAprendiz,
                nuevoInasistente,
                version + 1,
                creadaEn
        );
    }

    private void exigirEstado(EstadoSesion estadoRequerido) {
        if (estado != estadoRequerido) {
            throw new SesionInvalidaException(
                    "La transición requiere estado " + estadoRequerido + ", estado actual: "
                            + estado);
        }
    }

    private void validarActorParticipante(Persona actor) {
        if (!mismaPersona(actor, oferta.tutor())
                && !mismaPersona(actor, aprendiz)) {
            throw new SesionInvalidaException(
                    "El actor debe ser tutor o aprendiz de la sesión");
        }
    }

    private static boolean requiereAprendiz(EstadoSesion estado) {
        return estado != EstadoSesion.PROGRAMADA && estado != EstadoSesion.CANCELADA;
    }

    private static void validarPlazo(Duration plazo, String nombre) {
        if (plazo == null || plazo.isNegative() || plazo.isZero()) {
            throw new SesionInvalidaException(nombre + " debe ser mayor que cero");
        }
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
