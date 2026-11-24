package com.bancotiempo.pasaportesaberes.application.port.out;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface Reloj {
    OffsetDateTime ahora();

    LocalDate hoy();
}
