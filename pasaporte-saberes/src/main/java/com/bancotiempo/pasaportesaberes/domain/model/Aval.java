package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.AvalInvalidoException;

import java.time.LocalDate;

public record Aval(
    Long id,
    EntidadAliada entidad,
    Credencial credencial,
    LocalDate fecha,
    LocalDate vigenciaHasta,
    EstadoAval estado
) {
    public enum EstadoAval {
        VIGENTE,
        REVOCADO
    }

    public Aval {
        if (entidad == null || credencial == null || fecha == null
                || vigenciaHasta == null || estado == null) {
            throw new AvalInvalidoException(
                    "La entidad, credencial, fechas y estado son obligatorios");
        }
        if (vigenciaHasta.isBefore(fecha)) {
            throw new AvalInvalidoException(
                    "La vigencia del aval no puede terminar antes de su fecha de emisión");
        }
    }

    public boolean estaVigenteEn(LocalDate fechaActual) {
        if (fechaActual == null) {
            throw new AvalInvalidoException("La fecha actual es obligatoria");
        }
        return estado == EstadoAval.VIGENTE
                && !fechaActual.isBefore(fecha)
                && !fechaActual.isAfter(vigenciaHasta);
    }

    public Aval revocar() {
        if (estado == EstadoAval.REVOCADO) {
            return this;
        }
        return new Aval(
                id,
                entidad,
                credencial,
                fecha,
                vigenciaHasta,
                EstadoAval.REVOCADO
        );
    }
}
