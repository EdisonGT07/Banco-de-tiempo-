package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.RangoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

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

    public Rango {
        if (escala == null || nombre == null || nombre.isBlank() || orden <= 0) {
            throw new RangoInvalidoException(
                    "La escala, el nombre y un orden positivo son obligatorios");
        }
        minimoHoras = normalizarDecimal(minimoHoras, 8, 2, "Las horas mínimas");
        if (minimoHoras.signum() < 0) {
            throw new RangoInvalidoException("Las horas mínimas no pueden ser negativas");
        }
        minimoRating = normalizarDecimalOpcional(minimoRating, 3, 2, "El rating mínimo");
        if (minimoRating != null
                && (minimoRating.compareTo(BigDecimal.ONE) < 0
                || minimoRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new RangoInvalidoException("El rating mínimo debe estar entre 1 y 5");
        }
        validarMinimoConteo(minimoAprendices, "Los aprendices mínimos");
        validarMinimoConteo(minimoHabilidades, "Las habilidades mínimas");
        minimoAsistencia =
                normalizarDecimalOpcional(minimoAsistencia, 4, 3, "La asistencia mínima");
        if (minimoAsistencia != null
                && (minimoAsistencia.signum() < 0
                || minimoAsistencia.compareTo(BigDecimal.ONE) > 0)) {
            throw new RangoInvalidoException(
                    "La asistencia mínima debe estar entre 0 y 1");
        }
    }

    public boolean cumpleUmbrales(
            BigDecimal horas,
            BigDecimal rating,
            Integer aprendices,
            Integer habilidades,
            BigDecimal asistencia
    ) {
        return alcanza(horas, minimoHoras)
                && alcanza(rating, minimoRating)
                && alcanza(aprendices, minimoAprendices)
                && alcanza(habilidades, minimoHabilidades)
                && alcanza(asistencia, minimoAsistencia);
    }

    public static Optional<Rango> seleccionarRango(
            List<Rango> catalogo,
            EscalaRango escala,
            BigDecimal horas,
            BigDecimal rating,
            Integer aprendices,
            Integer habilidades,
            BigDecimal asistencia
    ) {
        if (catalogo == null || escala == null) {
            throw new RangoInvalidoException("El catálogo y la escala son obligatorios");
        }
        validarEstadisticas(horas, rating, aprendices, habilidades, asistencia);

        Rango mejorRango = null;
        for (Rango rango : catalogo) {
            if (rango == null) {
                throw new RangoInvalidoException(
                        "El catálogo de rangos contiene un valor nulo");
            }
            if (rango.escala() == escala && rango.cumpleUmbrales(
                    horas, rating, aprendices, habilidades, asistencia
            ) && (mejorRango == null || rango.orden() > mejorRango.orden())) {
                mejorRango = rango;
            }
        }
        return Optional.ofNullable(mejorRango);
    }

    private static boolean alcanza(BigDecimal valor, BigDecimal minimo) {
        return minimo == null || valor != null && valor.compareTo(minimo) >= 0;
    }

    private static boolean alcanza(Integer valor, Integer minimo) {
        return minimo == null || valor != null && valor >= minimo;
    }

    private static BigDecimal normalizarDecimal(
            BigDecimal valor,
            int precision,
            int escala,
            String nombre
    ) {
        if (valor == null) {
            throw new RangoInvalidoException(nombre + " son obligatorias");
        }
        BigDecimal normalizado = normalizarDecimalOpcional(valor, precision, escala, nombre);
        return normalizado;
    }

    private static BigDecimal normalizarDecimalOpcional(
            BigDecimal valor,
            int precision,
            int escala,
            String nombre
    ) {
        if (valor == null) {
            return null;
        }
        BigDecimal normalizado;
        try {
            normalizado = valor.setScale(escala, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new RangoInvalidoException(
                    nombre + " exceden la escala permitida");
        }
        if (normalizado.precision() > precision) {
            throw new RangoInvalidoException(
                    nombre + " exceden la precisión NUMERIC(" + precision + "," + escala + ")");
        }
        return normalizado;
    }

    private static void validarMinimoConteo(Integer valor, String nombre) {
        if (valor != null && valor < 0) {
            throw new RangoInvalidoException(nombre + " no pueden ser negativos");
        }
    }

    private static void validarEstadisticas(
            BigDecimal horas,
            BigDecimal rating,
            Integer aprendices,
            Integer habilidades,
            BigDecimal asistencia
    ) {
        if (horas != null && horas.signum() < 0
                || aprendices != null && aprendices < 0
                || habilidades != null && habilidades < 0
                || rating != null && (rating.compareTo(BigDecimal.ONE) < 0
                || rating.compareTo(BigDecimal.valueOf(5)) > 0)
                || asistencia != null && (asistencia.signum() < 0
                || asistencia.compareTo(BigDecimal.ONE) > 0)) {
            throw new RangoInvalidoException("Las estadísticas recibidas son inválidas");
        }
    }
}
