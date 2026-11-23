package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.TransaccionInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransaccionTest {
    private final Persona persona = new Persona();
    private final Billetera emisor = new Billetera(
            1L,
            Billetera.TipoBilletera.EMISOR,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0
    );
    private final Billetera usuario = new Billetera(
            2L,
            Billetera.TipoBilletera.USUARIO,
            persona,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0
    );

    @Test
    void creaUnBonoConDatosValidos() {
        Transaccion transaccion = new Transaccion(
                null,
                Transaccion.TipoTransaccion.BONO,
                new BigDecimal("2"),
                emisor,
                usuario,
                null,
                "bono-2",
                OffsetDateTime.now()
        );

        assertThat(transaccion.claveIdempotencia()).isEqualTo("bono-2");
        assertThat(transaccion.horas()).isEqualByComparingTo("2");
    }

    @Test
    void rechazaHorasCero() {
        assertThatThrownBy(() -> new Transaccion(
                null,
                Transaccion.TipoTransaccion.BONO,
                BigDecimal.ZERO,
                emisor,
                usuario,
                null,
                "bono-cero",
                OffsetDateTime.now()
        )).isInstanceOf(TransaccionInvalidaException.class);
    }

    @Test
    void rechazaOrigenIgualDestinoFueraDeReservaYLiberacion() {
        assertThatThrownBy(() -> new Transaccion(
                null,
                Transaccion.TipoTransaccion.GANADA,
                BigDecimal.ONE,
                usuario,
                usuario,
                null,
                "ganada-invalida",
                OffsetDateTime.now()
        )).isInstanceOf(TransaccionInvalidaException.class);
    }

    @Test
    void rechazaClaveDeIdempotenciaVacia() {
        assertThatThrownBy(() -> new Transaccion(
                null,
                Transaccion.TipoTransaccion.BONO,
                BigDecimal.ONE,
                emisor,
                usuario,
                null,
                " ",
                OffsetDateTime.now()
        )).isInstanceOf(TransaccionInvalidaException.class);
    }
}
