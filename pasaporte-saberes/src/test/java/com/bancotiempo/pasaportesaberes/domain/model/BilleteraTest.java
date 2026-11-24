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
    void consumirHorasReservadasDebeIncrementarLaVersion() {
        Billetera billetera = usuarioConSaldo("10");
        billetera.reservar(new BigDecimal("3"));

        billetera.debitarReservado(new BigDecimal("2"));

        assertThat(billetera.getSaldoReservado()).isEqualByComparingTo("1");
        assertThat(billetera.getVersion()).isEqualTo(2);
    }

    @Test
    void noPermiteReservarHorasEnBilleteraDeSistema() {
        Billetera fondo = billeteraSistema(Billetera.TipoBilletera.FONDO);

        assertThatThrownBy(() -> fondo.reservar(BigDecimal.ONE))
                .isInstanceOf(SaldoInvalidoException.class);
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

    @Test
    void horasDebenRespetarLaPrecisionDeLaBaseDeDatos() {
        Billetera billetera = usuarioConSaldo("10");

        assertThatThrownBy(() -> billetera.acreditar(new BigDecimal("1.001")))
                .isInstanceOf(SaldoInvalidoException.class);
        assertThatThrownBy(() -> billetera.acreditar(new BigDecimal("1000000.00")))
                .isInstanceOf(SaldoInvalidoException.class);
        assertThat(billetera.getSaldoDisponible()).isEqualByComparingTo("10");
    }

    @Test
    void saldoEnExcesoDePrecisionNoSeAcredita() {
        Billetera billetera = usuarioConSaldo("999999.99");

        assertThatThrownBy(() -> billetera.acreditar(new BigDecimal("0.01")))
                .isInstanceOf(SaldoInvalidoException.class);
        assertThat(billetera.getSaldoDisponible()).isEqualByComparingTo("999999.99");
    }

    @Test
    void transferirHorasReservadasConservaElTotalDeHoras() {
        Billetera aprendiz = usuarioConSaldo("5");
        Billetera tutor = new Billetera(
                11L,
                Billetera.TipoBilletera.USUARIO,
                new Persona(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );
        BigDecimal totalAntes = total(aprendiz, tutor);

        aprendiz.reservar(new BigDecimal("2"));
        aprendiz.debitarReservado(new BigDecimal("2"));
        tutor.acreditar(new BigDecimal("2"));

        assertThat(total(aprendiz, tutor)).isEqualByComparingTo(totalAntes);
    }

    @Test
    void noPermiteSaldoReservadoNegativoAlConstruirLaBilletera() {
        assertThatThrownBy(() -> new Billetera(
                10L,
                Billetera.TipoBilletera.USUARIO,
                persona,
                BigDecimal.ZERO,
                new BigDecimal("-1"),
                0
        )).isInstanceOf(SaldoInvalidoException.class);
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

    private Billetera billeteraSistema(Billetera.TipoBilletera tipo) {
        return new Billetera(
                20L,
                tipo,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );
    }

    private BigDecimal total(Billetera... billeteras) {
        BigDecimal total = BigDecimal.ZERO;
        for (Billetera billetera : billeteras) {
            total = total.add(billetera.getSaldoDisponible())
                    .add(billetera.getSaldoReservado());
        }
        return total;
    }
}
