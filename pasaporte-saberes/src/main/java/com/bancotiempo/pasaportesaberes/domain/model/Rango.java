package com.bancotiempo.pasaportesaberes.domain.model;

import java.math.BigDecimal;

public record Rango(
    Long id,
    EscalaRango escala,
    String nombre,
    short orden,
    BigDecimal minimoHoras,
    BigDecimal minimoRating,
    Integer minimoAprendices,
    Integer minimoHabilidades,
    BigDecimal minimoAsistencia
) {
    public enum EscalaRango {
        ENSENANZA,
        APRENDIZAJE
    }
}
