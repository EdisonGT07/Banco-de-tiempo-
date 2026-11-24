package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInsuficienteException;
import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInvalidoException;

import java.math.BigDecimal;
import java.util.Objects;

public class Billetera {
    public enum TipoBilletera {
        USUARIO,
        FONDO,
        EMISOR
    }

    private final Long id;
    private final TipoBilletera tipo;
    private final Persona persona;
    private BigDecimal saldoDisponible;
    private BigDecimal saldoReservado;
    private long version;

    public Billetera(Long id, TipoBilletera tipo, Persona persona,
                     BigDecimal saldoDisponible, BigDecimal saldoReservado, long version) {
        this.id = id;
        this.tipo = Objects.requireNonNull(tipo, "El tipo de billetera es obligatorio");
        this.persona = persona;
        this.saldoDisponible = normalizar(saldoDisponible);
        this.saldoReservado = normalizar(saldoReservado);
        this.version = version;

        validarIdentidad();
        validarSaldos();
    }

    public void reservar(BigDecimal horas) {
        validarBilleteraDeUsuario();
        validarHoras(horas);
        if (saldoDisponible.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoDisponible);
        }

        saldoDisponible = saldoDisponible.subtract(horas);
        saldoReservado = saldoReservado.add(horas);
        version++;
    }

    public void liberar(BigDecimal horas) {
        validarBilleteraDeUsuario();
        validarHoras(horas);
        if (saldoReservado.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoReservado);
        }

        saldoReservado = saldoReservado.subtract(horas);
        saldoDisponible = saldoDisponible.add(horas);
        version++;
    }

    public void acreditar(BigDecimal horas) {
        validarHoras(horas);
        saldoDisponible = saldoDisponible.add(horas);
        validarSaldos();
        version++;
    }

    public void debitar(BigDecimal horas) {
        validarHoras(horas);
        if (tipo != TipoBilletera.EMISOR && saldoDisponible.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoDisponible);
        }

        saldoDisponible = saldoDisponible.subtract(horas);
        validarSaldos();
        version++;
    }

    public void debitarReservado(BigDecimal horas) {
        validarBilleteraDeUsuario();
        validarHoras(horas);
        if (saldoReservado.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoReservado);
        }

        saldoReservado = saldoReservado.subtract(horas);
        version++;
    }

    private void validarBilleteraDeUsuario() {
        if (tipo != TipoBilletera.USUARIO) {
            throw new SaldoInvalidoException(
                    "Esta operación solo está permitida en una billetera USUARIO");
        }
    }

    private void validarIdentidad() {
        if (tipo == TipoBilletera.USUARIO && persona == null) {
            throw new SaldoInvalidoException("Una billetera USUARIO debe tener una persona");
        }
        if (tipo != TipoBilletera.USUARIO && persona != null) {
            throw new SaldoInvalidoException("Las billeteras EMISOR y FONDO no deben tener persona");
        }
    }

    private void validarSaldos() {
        if (saldoReservado.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInvalidoException("El saldo reservado no puede ser negativo");
        }
        if (tipo != TipoBilletera.EMISOR
                && saldoDisponible.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInvalidoException("El saldo disponible no puede ser negativo");
        }
    }

    private static void validarHoras(BigDecimal horas) {
        if (horas == null || horas.signum() <= 0) {
            throw new SaldoInvalidoException("Las horas deben ser mayores que cero");
        }
    }

    private static BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    public Long getId() {
        return id;
    }

    public TipoBilletera getTipo() {
        return tipo;
    }

    public Persona getPersona() {
        return persona;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public BigDecimal getSaldoReservado() {
        return saldoReservado;
    }

    public long getVersion() {
        return version;
    }
}
