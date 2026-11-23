package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInsuficienteException;
import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BilleteraTest {
    private final Persona persona = new Persona();

    @Test
    void reservaDebeMoverHorasADisponibleAReservado() {
        Billetera billetera = usuarioConSaldo("10");

        billetera.reservar(new BigDecimal("4"));

        assertThat(billetera.getSaldoDisponible()).isEqualByComparingTo("6");
        assertThat(billetera.getSaldoReservado()).isEqualByComparingTo("4");
        assertThat(billetera.getVersion()).isEqualTo(1);
    }

    @Test
    void reservaDebeFallarSinSaldoSuficiente() {
        Billetera billetera = usuarioConSaldo("2");

        assertThatThrownBy(() -> billetera.reservar(new BigDecimal("3")))
                .isInstanceOf(SaldoInsuficienteException.class);
    }

    @Test
    void liberarDebeDevolverHorasDelReservadoADisponible() {
        Billetera billetera = usuarioConSaldo("10");
        billetera.reservar(new BigDecimal("3"));

        billetera.liberar(new BigDecimal("3"));

        assertThat(billetera.getSaldoDisponible()).isEqualByComparingTo("10");
        assertThat(billetera.getSaldoReservado()).isEqualByComparingTo("0");
    }

    @Test
    void fondoNoPuedeQuedarConSaldoDisponibleNegativo() {
        Billetera fondo = new Billetera(
                2L,
                Billetera.TipoBilletera.FONDO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );

        assertThatThrownBy(() -> fondo.debitar(new BigDecimal("1")))
                .isInstanceOf(SaldoInsuficienteException.class);
    }

    @Test
    void emisorPuedeEntregarHorasNegativasYUsuarioNo() {
        Billetera emisor = new Billetera(
                1L,
                Billetera.TipoBilletera.EMISOR,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );

        emisor.debitar(new BigDecimal("2"));

        assertThat(emisor.getSaldoDisponible()).isEqualByComparingTo("-2");
    }

    @Test
    void horasNoPositivasNoSeAceptan() {
        Billetera billetera = usuarioConSaldo("10");

        assertThatThrownBy(() -> billetera.acreditar(BigDecimal.ZERO))
                .isInstanceOf(SaldoInvalidoException.class);
    }

    private Billetera usuarioConSaldo(String saldo) {
        return new Billetera(
                10L,
                Billetera.TipoBilletera.USUARIO,
                persona,
                new BigDecimal(saldo),
                BigDecimal.ZERO,
                0
        );
    }
}
