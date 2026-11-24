package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.TransaccionInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransaccionTest {
    private final Persona persona = new Persona();
    private final Persona otraPersona = new Persona();
    private final Billetera emisor = new Billetera(
            1L,
            Billetera.TipoBilletera.EMISOR,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0
    );
    private final Billetera otroUsuario = new Billetera(
            3L,
            Billetera.TipoBilletera.USUARIO,
            otraPersona,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0
    );
    private final Billetera fondo = new Billetera(
            4L,
            Billetera.TipoBilletera.FONDO,
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0
    );
    private final Sesion sesion = new Sesion(
            5L,
            null,
            null,
            null,
            null,
            Sesion.EstadoSesion.COMPLETADA,
            null,
            null,
            null,
            0,
            null
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
    void reservaYLiberacionDebenSerEntreLosDosSaldosDeLaMismaBilleteraUsuario() {
        assertThatThrownBy(() -> transaccion(
                Transaccion.TipoTransaccion.RESERVA, usuario, otroUsuario, sesion, "reserva"
        )).isInstanceOf(TransaccionInvalidaException.class);

        assertThat(transaccion(
                Transaccion.TipoTransaccion.LIBERACION, usuario, usuario, sesion, "liberacion"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.LIBERACION);
    }

    @Test
    void bonoDebeMoverHorasDesdeEmisorHaciaUsuario() {
        assertThat(transaccion(
                Transaccion.TipoTransaccion.BONO, emisor, usuario, null, "bono"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.BONO);

        assertThatThrownBy(() -> transaccion(
                Transaccion.TipoTransaccion.BONO, usuario, otroUsuario, null, "bono-invalido"
        )).isInstanceOf(TransaccionInvalidaException.class);
    }

    @Test
    void ganadaDebeMoverHorasEntreDosUsuariosYRequerirSesion() {
        assertThat(transaccion(
                Transaccion.TipoTransaccion.GANADA, usuario, otroUsuario, sesion, "ganada"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.GANADA);

        assertThatThrownBy(() -> transaccion(
                Transaccion.TipoTransaccion.GANADA, usuario, otroUsuario, null, "ganada-sin-sesion"
        )).isInstanceOf(TransaccionInvalidaException.class);
    }

    @Test
    void donadaDebeIrAlFondoOaOtroUsuario() {
        assertThat(transaccion(
                Transaccion.TipoTransaccion.DONADA, usuario, fondo, sesion, "donada-fondo"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.DONADA);
        assertThat(transaccion(
                Transaccion.TipoTransaccion.DONADA, usuario, otroUsuario, sesion, "donada-usuario"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.DONADA);

        assertThatThrownBy(() -> transaccion(
                Transaccion.TipoTransaccion.DONADA, usuario, emisor, sesion, "donada-emisor"
        )).isInstanceOf(TransaccionInvalidaException.class);
    }

    @Test
    void excedenteYRetiroDebenUsarLaBilleteraFondo() {
        assertThat(transaccion(
                Transaccion.TipoTransaccion.EXCEDENTE, usuario, fondo, sesion, "excedente"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.EXCEDENTE);
        assertThat(transaccion(
                Transaccion.TipoTransaccion.RETIRO_FONDO, fondo, usuario, null, "retiro"
        ).tipo()).isEqualTo(Transaccion.TipoTransaccion.RETIRO_FONDO);

        assertThatThrownBy(() -> transaccion(
                Transaccion.TipoTransaccion.RETIRO_FONDO, usuario, fondo, null, "retiro-invalido"
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

    private Transaccion transaccion(
            Transaccion.TipoTransaccion tipo,
            Billetera origen,
            Billetera destino,
            Sesion sesion,
            String clave
    ) {
        return new Transaccion(
                null,
                tipo,
                BigDecimal.ONE,
                origen,
                destino,
                sesion,
                clave,
                OffsetDateTime.now()
        );
    }
}
