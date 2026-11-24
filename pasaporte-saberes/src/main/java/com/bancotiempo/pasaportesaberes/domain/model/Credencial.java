package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.CredencialInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Credencial(
    Long id,
    Persona persona,
    Habilidad habilidad,
    BigDecimal horasEnsenadas,
    BigDecimal ratingPromedio,
    String codigoVerificacion,
    String firma,
    OffsetDateTime actualizadaEn,
    long numeroVerificaciones
) {
    public enum Nivel {
        REGISTRADA,
        VALIDADA_POR_PARES,
        AVALADA
    }

    public Credencial {
        if (persona == null || habilidad == null) {
            throw new CredencialInvalidaException(
                    "La persona y la habilidad son obligatorias");
        }
        horasEnsenadas = normalizarHoras(horasEnsenadas);
        if (horasEnsenadas.signum() < 0) {
            throw new CredencialInvalidaException(
                    "Las horas enseñadas no pueden ser negativas");
        }
        ratingPromedio = normalizarRating(ratingPromedio);
        if (codigoVerificacion == null || codigoVerificacion.isBlank()
                || codigoVerificacion.length() > 32) {
            throw new CredencialInvalidaException(
                    "El código de verificación debe tener entre 1 y 32 caracteres");
        }
        if (firma == null || firma.isBlank() || actualizadaEn == null) {
            throw new CredencialInvalidaException(
                    "La firma y la fecha de actualización son obligatorias");
        }
        if (numeroVerificaciones < 0) {
            throw new CredencialInvalidaException(
                    "El número de verificaciones no puede ser negativo");
        }
    }

    public Credencial actualizarEstadisticas(
            BigDecimal nuevasHorasEnsenadas,
            BigDecimal nuevoRatingPromedio,
            String nuevaFirma,
            OffsetDateTime actualizadaEn
    ) {
        return new Credencial(
                id,
                persona,
                habilidad,
                nuevasHorasEnsenadas,
                nuevoRatingPromedio,
                codigoVerificacion,
                nuevaFirma,
                actualizadaEn,
                numeroVerificaciones
        );
    }

    public Credencial registrarVerificacion(OffsetDateTime verificadaEn) {
        if (verificadaEn == null) {
            throw new CredencialInvalidaException(
                    "La fecha de verificación es obligatoria");
        }
        if (numeroVerificaciones == Long.MAX_VALUE) {
            throw new CredencialInvalidaException(
                    "El número de verificaciones alcanzó su máximo");
        }
        return new Credencial(
                id,
                persona,
                habilidad,
                horasEnsenadas,
                ratingPromedio,
                codigoVerificacion,
                firma,
                verificadaEn,
                numeroVerificaciones + 1
        );
    }

    public BigDecimal calcularRatingPonderado(List<Resena> resenasRecibidas) {
        if (resenasRecibidas == null) {
            throw new CredencialInvalidaException(
                    "La lista de reseñas recibidas es obligatoria");
        }
        List<Resena> ordenadas = new ArrayList<>(resenasRecibidas);
        if (ordenadas.stream().anyMatch(resena -> resena == null)) {
            throw new CredencialInvalidaException(
                    "La lista de reseñas contiene un valor nulo");
        }
        ordenadas.sort(Comparator.comparing(Resena::creadaEn)
                .thenComparing(
                        Resena::id,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ));

        List<Persona> aprendicesContados = new ArrayList<>();
        List<Integer> cantidadesPorAprendiz = new ArrayList<>();
        BigDecimal sumaPonderada = BigDecimal.ZERO;
        BigDecimal sumaPesos = BigDecimal.ZERO;

        for (Resena resena : ordenadas) {
            if (resena.autorRol() != Resena.AutorRol.APRENDIZ
                    || !mismaPersona(resena.sesion().oferta().tutor(), persona)) {
                continue;
            }

            Persona aprendiz = resena.sesion().aprendiz();
            int indice = indiceDeAprendiz(aprendicesContados, aprendiz);
            int cantidadPrevia = indice < 0 ? 0 : cantidadesPorAprendiz.get(indice);
            BigDecimal peso = pesoPara(cantidadPrevia);

            if (mismaHabilidad(resena.sesion().oferta().habilidad(), habilidad)) {
                sumaPonderada = sumaPonderada.add(
                        BigDecimal.valueOf(resena.calificacion()).multiply(peso)
                );
                sumaPesos = sumaPesos.add(peso);
            }

            if (indice < 0) {
                aprendicesContados.add(aprendiz);
                cantidadesPorAprendiz.add(1);
            } else {
                cantidadesPorAprendiz.set(indice, cantidadPrevia + 1);
            }
        }

        if (sumaPesos.signum() == 0) {
            return null;
        }
        return sumaPonderada.divide(sumaPesos, 2, RoundingMode.HALF_UP);
    }

    public Nivel calcularNivel(
            int aprendicesDistintos,
            BigDecimal ratingMinimo,
            int aprendicesMinimos,
            Aval aval,
            LocalDate fechaActual
    ) {
        if (aprendicesDistintos < 0 || aprendicesMinimos <= 0) {
            throw new CredencialInvalidaException(
                    "Los aprendices deben ser cantidades válidas");
        }
        BigDecimal minimo = normalizarRating(ratingMinimo);
        if (minimo == null || fechaActual == null) {
            throw new CredencialInvalidaException(
                    "El rating mínimo y la fecha actual son obligatorios");
        }
        if (aval != null && !mismoAvalDeEstaCredencial(aval)) {
            throw new CredencialInvalidaException(
                    "El aval debe corresponder a esta credencial");
        }
        if (aval != null && aval.estaVigenteEn(fechaActual)) {
            return Nivel.AVALADA;
        }
        if (ratingPromedio != null
                && ratingPromedio.compareTo(minimo) >= 0
                && aprendicesDistintos >= aprendicesMinimos) {
            return Nivel.VALIDADA_POR_PARES;
        }
        return Nivel.REGISTRADA;
    }

    private boolean mismoAvalDeEstaCredencial(Aval aval) {
        Credencial avalada = aval.credencial();
        if (avalada == this) {
            return true;
        }
        return id != null && id.equals(avalada.id());
    }

    private static BigDecimal pesoPara(int cantidadPrevia) {
        return switch (cantidadPrevia) {
            case 0 -> BigDecimal.ONE;
            case 1 -> new BigDecimal("0.50");
            default -> new BigDecimal("0.25");
        };
    }

    private static int indiceDeAprendiz(List<Persona> aprendices, Persona buscado) {
        for (int indice = 0; indice < aprendices.size(); indice++) {
            if (mismaPersona(aprendices.get(indice), buscado)) {
                return indice;
            }
        }
        return -1;
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

    private static boolean mismaHabilidad(Habilidad primera, Habilidad segunda) {
        if (primera == segunda) {
            return true;
        }
        return primera.id() != null && primera.id().equals(segunda.id());
    }

    private static BigDecimal normalizarHoras(BigDecimal horas) {
        if (horas == null) {
            throw new CredencialInvalidaException(
                    "Las horas enseñadas son obligatorias");
        }
        BigDecimal normalizadas;
        try {
            normalizadas = horas.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new CredencialInvalidaException(
                    "Las horas deben tener como máximo dos decimales");
        }
        if (normalizadas.precision() > 8) {
            throw new CredencialInvalidaException(
                    "Las horas exceden la precisión NUMERIC(8,2)");
        }
        return normalizadas;
    }

    private static BigDecimal normalizarRating(BigDecimal rating) {
        if (rating == null) {
            return null;
        }
        BigDecimal normalizado;
        try {
            normalizado = rating.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new CredencialInvalidaException(
                    "El rating debe tener como máximo dos decimales");
        }
        if (normalizado.precision() > 3
                || normalizado.compareTo(BigDecimal.ONE) < 0
                || normalizado.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new CredencialInvalidaException(
                    "El rating debe estar entre 1 y 5 con precisión NUMERIC(3,2)");
        }
        return normalizado;
    }
}
