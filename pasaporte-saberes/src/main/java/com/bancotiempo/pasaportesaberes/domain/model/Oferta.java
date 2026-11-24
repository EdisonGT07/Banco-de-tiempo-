package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.OfertaInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

public record Oferta(
    Long id,
    Persona tutor,
    Habilidad habilidad,
    String descripcion,
    BigDecimal duracionHoras,
    Modalidad modalidad,
    String barrio,
    DestinoHoras destinoHoras,
    Persona receptorRegalo,
    boolean activa,
    OffsetDateTime creadaEn
) {
    public enum Modalidad {
        PRESENCIAL,
        VIRTUAL
    }

    public enum DestinoHoras {
        BILLETERA_PROPIA,
        FONDO,
        REGALO
    }

    public Oferta {
        if (tutor == null || habilidad == null) {
            throw new OfertaInvalidaException("El tutor y la habilidad son obligatorios");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new OfertaInvalidaException("La descripción de la oferta es obligatoria");
        }
        duracionHoras = normalizarDuracion(duracionHoras);
        if (duracionHoras.signum() <= 0) {
            throw new OfertaInvalidaException("La duración debe ser mayor que cero");
        }
        if (modalidad == null || destinoHoras == null || creadaEn == null) {
            throw new OfertaInvalidaException(
                    "La modalidad, el destino y la fecha de creación son obligatorios");
        }
        if (destinoHoras == DestinoHoras.REGALO && receptorRegalo == null) {
            throw new OfertaInvalidaException("El destino REGALO requiere un receptor");
        }
        if (destinoHoras != DestinoHoras.REGALO && receptorRegalo != null) {
            throw new OfertaInvalidaException(
                    "Solo una oferta REGALO puede tener un receptor");
        }
        if (receptorRegalo != null && mismaPersona(tutor, receptorRegalo)) {
            throw new OfertaInvalidaException("El tutor no puede ser el receptor del regalo");
        }
    }

    public Oferta publicar() {
        return activa ? this : conEstado(true);
    }

    public Oferta desactivar() {
        return activa ? conEstado(false) : this;
    }

    private Oferta conEstado(boolean nuevoEstado) {
        return new Oferta(
                id,
                tutor,
                habilidad,
                descripcion,
                duracionHoras,
                modalidad,
                barrio,
                destinoHoras,
                receptorRegalo,
                nuevoEstado,
                creadaEn
        );
    }

    private static BigDecimal normalizarDuracion(BigDecimal duracionHoras) {
        if (duracionHoras == null) {
            throw new OfertaInvalidaException("La duración de la oferta es obligatoria");
        }
        BigDecimal normalizada;
        try {
            normalizada = duracionHoras.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new OfertaInvalidaException(
                    "La duración debe tener como máximo dos decimales");
        }
        if (normalizada.precision() > 4) {
            throw new OfertaInvalidaException(
                    "La duración excede la precisión NUMERIC(4,2)");
        }
        return normalizada;
    }

    private static boolean mismaPersona(Persona primera, Persona segunda) {
        if (primera == segunda) {
            return true;
        }
        return primera.getId() != null && primera.getId().equals(segunda.getId());
    }
}
