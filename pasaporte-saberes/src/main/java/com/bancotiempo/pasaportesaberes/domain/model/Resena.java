package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.OffsetDateTime;

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
}
