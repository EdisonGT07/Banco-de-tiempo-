package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.HabilidadInvalidaException;

public record Habilidad(Long id, String nombre, String categoria) {
    public Habilidad {
        if (id != null && id <= 0) {
            throw new HabilidadInvalidaException("El id de habilidad debe ser positivo");
        }
        if (nombre == null || nombre.isBlank() || nombre.length() > 80) {
            throw new HabilidadInvalidaException(
                    "El nombre es obligatorio y no puede superar 80 caracteres");
        }
        if (categoria == null || categoria.isBlank() || categoria.length() > 60) {
            throw new HabilidadInvalidaException(
                    "La categoría es obligatoria y no puede superar 60 caracteres");
        }
    }
}
