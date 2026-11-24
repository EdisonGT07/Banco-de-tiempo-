package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.ResenaInvalidaException;

import java.time.OffsetDateTime;
import java.util.List;

public record Resena(
    Long id,
    Sesion sesion,
    AutorRol autorRol,
    short calificacion,
    String comentario,
    OffsetDateTime creadaEn
) {
    public enum AutorRol {
        TUTOR,
        APRENDIZ
    }

    public Resena {
        if (sesion == null || autorRol == null || creadaEn == null) {
            throw new ResenaInvalidaException(
                    "La sesión, el rol del autor y la fecha son obligatorios");
        }
        if (sesion.estado() != Sesion.EstadoSesion.COMPLETADA) {
            throw new ResenaInvalidaException(
                    "Solo se puede calificar una sesión COMPLETADA");
        }
        if (calificacion < 1 || calificacion > 5) {
            throw new ResenaInvalidaException("La calificación debe estar entre 1 y 5");
        }
    }

    public static Resena crear(
            Long id,
            Sesion sesion,
            Persona autor,
            short calificacion,
            String comentario,
            OffsetDateTime creadaEn
    ) {
        if (sesion == null || autor == null) {
            throw new ResenaInvalidaException("La sesión y el autor son obligatorios");
        }
        Resena.AutorRol autorRol;
        if (mismaPersona(autor, sesion.oferta().tutor())) {
            autorRol = AutorRol.TUTOR;
        } else if (mismaPersona(autor, sesion.aprendiz())) {
            autorRol = AutorRol.APRENDIZ;
        } else {
            throw new ResenaInvalidaException(
                    "Solo el tutor o el aprendiz pueden crear una reseña");
        }
        return new Resena(id, sesion, autorRol, calificacion, comentario, creadaEn);
    }

    public Persona autor() {
        return autorRol == AutorRol.TUTOR
                ? sesion.oferta().tutor()
                : sesion.aprendiz();
    }

    public void validarUnica(List<Resena> resenasExistentes) {
        if (resenasExistentes == null) {
            throw new ResenaInvalidaException("La lista de reseñas existentes es obligatoria");
        }
        for (Resena existente : resenasExistentes) {
            if (existente == null) {
                throw new ResenaInvalidaException(
                        "La lista de reseñas contiene un valor nulo");
            }
            if (existente.autorRol == autorRol && mismaSesion(existente.sesion, sesion)) {
                throw new ResenaInvalidaException(
                        "Este participante ya calificó la sesión");
            }
        }
    }

    private static boolean mismaSesion(Sesion primera, Sesion segunda) {
        if (primera == segunda) {
            return true;
        }
        return primera.id() != null && primera.id().equals(segunda.id());
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
