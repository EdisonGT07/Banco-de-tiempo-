package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransaccionAplicacionTest {
    private final Persona aprendiz = new Persona();
    private final Persona tutor = new Persona();
    private final OffsetDateTime ahora = OffsetDateTime.now();
    private final Oferta oferta = new Oferta(
            1L,
            tutor,
            new Habilidad(1L, "Habilidad de prueba", "General"),
            "Oferta de prueba",
            BigDecimal.ONE,
            Oferta.Modalidad.VIRTUAL,
            null,
            Oferta.DestinoHoras.BILLETERA_PROPIA,
            null,
            true,
            ahora
    );
    private final Sesion sesion = new Sesion(
            1L, oferta, aprendiz, ahora, null, Sesion.EstadoSesion.COMPLETADA,
            ahora, ahora, null, 0, ahora
    );

    @Test
    void aplicaBonoYConservaElTotalIncluyendoAlEmisor() {
        Billetera emisor = billetera(1L, Billetera.TipoBilletera.EMISOR, null, "0", "0");
        Billetera usuario = billetera(2L, Billetera.TipoBilletera.USUARIO, aprendiz, "0", "0");

        aplicar(Transaccion.TipoTransaccion.BONO, emisor, usuario, null, "3", "bono-1");

        assertThat(emisor.getSaldoDisponible()).isEqualByComparingTo("-3");
        assertThat(usuario.getSaldoDisponible()).isEqualByComparingTo("3");
        assertThat(total(emisor, usuario)).isEqualByComparingTo("0");
    }

    @Test
    void reservaYLiberacionMuevenSaldoDentroDeLaMismaBilletera() {
        Billetera usuario = billetera(1L, Billetera.TipoBilletera.USUARIO, aprendiz, "5", "0");

        aplicar(Transaccion.TipoTransaccion.RESERVA, usuario, usuario, sesion, "2", "reserva-1");
        assertThat(usuario.getSaldoDisponible()).isEqualByComparingTo("3");
        assertThat(usuario.getSaldoReservado()).isEqualByComparingTo("2");

        aplicar(Transaccion.TipoTransaccion.LIBERACION,
                usuario, usuario, sesion, "1", "liberacion-1");
        assertThat(usuario.getSaldoDisponible()).isEqualByComparingTo("4");
        assertThat(usuario.getSaldoReservado()).isEqualByComparingTo("1");
    }

    @Test
    void liquidacionGanadaTransfiereDesdeReservadoAlTutor() {
        Billetera origen = billetera(1L, Billetera.TipoBilletera.USUARIO, aprendiz, "0", "2");
        Billetera destino = billetera(2L, Billetera.TipoBilletera.USUARIO, tutor, "1", "0");

        aplicar(Transaccion.TipoTransaccion.GANADA, origen, destino, sesion, "2", "ganada-1");

        assertThat(origen.getSaldoReservado()).isEqualByComparingTo("0");
        assertThat(destino.getSaldoDisponible()).isEqualByComparingTo("3");
        assertThat(total(origen, destino)).isEqualByComparingTo("3");
    }

    @Test
    void donacionDesdeReservadoPuedeIrAlFondo() {
        Billetera origen = billetera(1L, Billetera.TipoBilletera.USUARIO, aprendiz, "0", "2");
        Billetera fondo = billetera(2L, Billetera.TipoBilletera.FONDO, null, "4", "0");

        aplicar(Transaccion.TipoTransaccion.DONADA, origen, fondo, sesion, "2", "donada-1");

        assertThat(origen.getSaldoReservado()).isEqualByComparingTo("0");
        assertThat(fondo.getSaldoDisponible()).isEqualByComparingTo("6");
        assertThat(total(origen, fondo)).isEqualByComparingTo("6");
    }

    @Test
    void excedenteYRetiroUsanSaldoDisponible() {
        Billetera usuario = billetera(1L, Billetera.TipoBilletera.USUARIO, tutor, "5", "0");
        Billetera fondo = billetera(2L, Billetera.TipoBilletera.FONDO, null, "4", "0");

        aplicar(Transaccion.TipoTransaccion.EXCEDENTE, usuario, fondo, sesion, "2", "excedente-1");
        assertThat(usuario.getSaldoDisponible()).isEqualByComparingTo("3");
        assertThat(fondo.getSaldoDisponible()).isEqualByComparingTo("6");

        aplicar(Transaccion.TipoTransaccion.RETIRO_FONDO, fondo, usuario, null, "1", "retiro-1");
        assertThat(fondo.getSaldoDisponible()).isEqualByComparingTo("5");
        assertThat(usuario.getSaldoDisponible()).isEqualByComparingTo("4");
        assertThat(total(usuario, fondo)).isEqualByComparingTo("9");
    }

    @Test
    void unaFallaAlValidarDestinoNoModificaNingunaBilletera() {
        Billetera origen = billetera(1L, Billetera.TipoBilletera.USUARIO, tutor, "5", "0");
        Billetera fondo = billetera(
                2L, Billetera.TipoBilletera.FONDO, null, "999999.99", "0"
        );
        long versionOrigen = origen.getVersion();
        long versionFondo = fondo.getVersion();

        assertThatThrownBy(() -> aplicar(
                Transaccion.TipoTransaccion.EXCEDENTE, origen, fondo, sesion, "1", "excedente-2"
        )).isInstanceOf(SaldoInvalidoException.class);

        assertThat(origen.getSaldoDisponible()).isEqualByComparingTo("5");
        assertThat(fondo.getSaldoDisponible()).isEqualByComparingTo("999999.99");
        assertThat(origen.getVersion()).isEqualTo(versionOrigen);
        assertThat(fondo.getVersion()).isEqualTo(versionFondo);
    }

    @Test
    void cadaMovimientoConservaElTotalContable() {
        Billetera emisor = billetera(1L, Billetera.TipoBilletera.EMISOR, null, "0", "0");
        Billetera aprendizWallet = billetera(
                2L, Billetera.TipoBilletera.USUARIO, aprendiz, "10", "0"
        );
        Billetera tutorWallet = billetera(
                3L, Billetera.TipoBilletera.USUARIO, tutor, "0", "0"
        );
        Billetera fondo = billetera(4L, Billetera.TipoBilletera.FONDO, null, "0", "0");
        BigDecimal totalInicial = total(emisor, aprendizWallet, tutorWallet, fondo);

        aplicar(Transaccion.TipoTransaccion.BONO, emisor, aprendizWallet, null, "2", "bono-2");
        aplicar(Transaccion.TipoTransaccion.RESERVA,
                aprendizWallet, aprendizWallet, sesion, "4", "reserva-2");
        aplicar(Transaccion.TipoTransaccion.LIBERACION,
                aprendizWallet, aprendizWallet, sesion, "1", "liberacion-2");
        aplicar(Transaccion.TipoTransaccion.GANADA,
                aprendizWallet, tutorWallet, sesion, "2", "ganada-2");
        aplicar(Transaccion.TipoTransaccion.DONADA,
                aprendizWallet, fondo, sesion, "1", "donada-2");
        aplicar(Transaccion.TipoTransaccion.EXCEDENTE,
                tutorWallet, fondo, sesion, "1", "excedente-3");
        aplicar(Transaccion.TipoTransaccion.RETIRO_FONDO,
                fondo, aprendizWallet, null, "1", "retiro-2");

        assertThat(total(emisor, aprendizWallet, tutorWallet, fondo))
                .isEqualByComparingTo(totalInicial);
    }

    private void aplicar(
            Transaccion.TipoTransaccion tipo,
            Billetera origen,
            Billetera destino,
            Sesion sesion,
            String horas,
            String clave
    ) {
        Transaccion transaccion = new Transaccion(
                null,
                tipo,
                new BigDecimal(horas),
                origen,
                destino,
                sesion,
                clave,
                ahora
        );
        transaccion.aplicar();
    }

    private Billetera billetera(
            Long id,
            Billetera.TipoBilletera tipo,
            Persona persona,
            String disponible,
            String reservado
    ) {
        return new Billetera(
                id,
                tipo,
                persona,
                new BigDecimal(disponible),
                new BigDecimal(reservado),
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
