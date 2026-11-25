package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.time;

import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Component
public final class RelojSistema implements Reloj {
    private final Clock clock;

    public RelojSistema(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public OffsetDateTime ahora() {
        return OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);
    }

    @Override
    public LocalDate hoy() {
        return LocalDate.now(clock);
    }
}
