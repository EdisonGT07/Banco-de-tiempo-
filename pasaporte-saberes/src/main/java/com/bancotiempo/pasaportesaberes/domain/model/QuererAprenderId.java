package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.QuererAprenderInvalidoException;

public record QuererAprenderId(Long personaId, Long habilidadId) {
    public QuererAprenderId {
        if (personaId == null || personaId <= 0 || habilidadId == null || habilidadId <= 0) {
            throw new QuererAprenderInvalidoException(
                    "Los ids de persona y habilidad deben ser positivos");
        }
    }
}
