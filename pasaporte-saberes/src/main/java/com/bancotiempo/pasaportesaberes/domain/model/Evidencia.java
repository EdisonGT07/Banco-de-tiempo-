package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.EvidenciaInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

public record Evidencia(
    Long id,
    Sesion sesion,
    String tokenHash,
    String codigoCortoHash,
    OffsetDateTime expiraEn,
    OffsetDateTime usadoEn,
    short intentosFallidos,
    String fotoUrl,
    BigDecimal latitud,
    BigDecimal longitud,
    OffsetDateTime marcaServidor
) {
    private static final short MAX_INTENTOS_FALLIDOS = 5;

    public Evidencia {
        if (sesion == null || !esHashSha256(tokenHash) || !esHashSha256(codigoCortoHash)) {
            throw new EvidenciaInvalidaException(
                    "La sesión y los hashes SHA-256 son obligatorios");
        }
        if (expiraEn == null || marcaServidor == null
                || !expiraEn.isAfter(marcaServidor)) {
            throw new EvidenciaInvalidaException(
                    "La expiración debe ser posterior a la marca del servidor");
        }
        if (intentosFallidos < 0 || intentosFallidos > MAX_INTENTOS_FALLIDOS) {
            throw new EvidenciaInvalidaException(
                    "Los intentos fallidos deben estar entre 0 y 5");
        }
        if (usadoEn != null
                && (usadoEn.isBefore(marcaServidor) || !usadoEn.isBefore(expiraEn))) {
            throw new EvidenciaInvalidaException(
                    "El consumo debe ocurrir antes de la expiración y después de su creación");
        }
        if ((latitud == null) != (longitud == null)) {
            throw new EvidenciaInvalidaException(
                    "La latitud y longitud deben informarse juntas");
        }
        if (latitud != null) {
            latitud = normalizarCoordenada(latitud, "La latitud");
            longitud = normalizarCoordenada(longitud, "La longitud");
            if (latitud.compareTo(BigDecimal.valueOf(-90)) < 0
                    || latitud.compareTo(BigDecimal.valueOf(90)) > 0) {
                throw new EvidenciaInvalidaException("La latitud debe estar entre -90 y 90");
            }
            if (longitud.compareTo(BigDecimal.valueOf(-180)) < 0
                    || longitud.compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new EvidenciaInvalidaException("La longitud debe estar entre -180 y 180");
            }
        }
    }

    public boolean estaConsumida() {
        return usadoEn != null;
    }

    public boolean estaBloqueadaPorIntentos() {
        return intentosFallidos >= MAX_INTENTOS_FALLIDOS;
    }

    public boolean estaExpiradaEn(OffsetDateTime ahora) {
        validarAhora(ahora);
        return !ahora.isBefore(expiraEn);
    }

    public Evidencia registrarIntentoFallido(OffsetDateTime ahora) {
        validarUsableEn(ahora);
        return new Evidencia(
                id,
                sesion,
                tokenHash,
                codigoCortoHash,
                expiraEn,
                usadoEn,
                (short) (intentosFallidos + 1),
                fotoUrl,
                latitud,
                longitud,
                marcaServidor
        );
    }

    public Evidencia consumir(OffsetDateTime ahora) {
        validarUsableEn(ahora);
        return new Evidencia(
                id,
                sesion,
                tokenHash,
                codigoCortoHash,
                expiraEn,
                ahora,
                intentosFallidos,
                fotoUrl,
                latitud,
                longitud,
                marcaServidor
        );
    }

    private void validarUsableEn(OffsetDateTime ahora) {
        validarAhora(ahora);
        if (estaConsumida()) {
            throw new EvidenciaInvalidaException("La evidencia ya fue consumida");
        }
        if (estaExpiradaEn(ahora)) {
            throw new EvidenciaInvalidaException("La evidencia ya expiró");
        }
        if (estaBloqueadaPorIntentos()) {
            throw new EvidenciaInvalidaException(
                    "La evidencia alcanzó el máximo de intentos fallidos");
        }
    }

    private static boolean esHashSha256(String hash) {
        return hash != null && hash.matches("[0-9a-fA-F]{64}");
    }

    private static BigDecimal normalizarCoordenada(BigDecimal coordenada, String nombre) {
        BigDecimal normalizada;
        try {
            normalizada = coordenada.setScale(6, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new EvidenciaInvalidaException(
                    nombre + " debe tener como máximo seis decimales");
        }
        if (normalizada.precision() > 9) {
            throw new EvidenciaInvalidaException(
                    nombre + " excede la precisión NUMERIC(9,6)");
        }
        return normalizada;
    }

    private static void validarAhora(OffsetDateTime ahora) {
        if (ahora == null) {
            throw new EvidenciaInvalidaException("La fecha actual es obligatoria");
        }
    }
}
