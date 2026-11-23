package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.OffsetDateTime;

public record QuererAprender(
    QuererAprenderId id,
    Persona persona,
    Habilidad habilidad,
    OffsetDateTime creadaEn
) {
}
