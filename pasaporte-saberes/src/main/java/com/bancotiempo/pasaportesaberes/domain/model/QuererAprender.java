package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.QuererAprenderInvalidoException;

import java.time.OffsetDateTime;

public record QuererAprender(
    QuererAprenderId id,
    Persona persona,
    Habilidad habilidad,
    OffsetDateTime creadaEn
) {
    public QuererAprender {
        if (id == null || persona == null || habilidad == null || creadaEn == null) {
            throw new QuererAprenderInvalidoException(
                    "El id, la persona, la habilidad y la fecha de creación son obligatorios");
        }
        if (!id.personaId().equals(persona.getId())
                || !id.habilidadId().equals(habilidad.id())) {
            throw new QuererAprenderInvalidoException(
                    "El id compuesto debe corresponder a la persona y habilidad asociadas");
        }
    }

    public static QuererAprender crear(
            Persona persona,
            Habilidad habilidad,
            OffsetDateTime creadaEn
    ) {
        if (persona == null || persona.getId() == null || habilidad == null
                || habilidad.id() == null) {
            throw new QuererAprenderInvalidoException(
                    "La persona y la habilidad deben estar identificadas");
        }
        return new QuererAprender(
                new QuererAprenderId(persona.getId(), habilidad.id()),
                persona,
                habilidad,
                creadaEn
        );
    }
}
