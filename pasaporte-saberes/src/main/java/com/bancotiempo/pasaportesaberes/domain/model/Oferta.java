package com.bancotiempo.pasaportesaberes.domain.model;

import java.math.BigDecimal;
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
}
