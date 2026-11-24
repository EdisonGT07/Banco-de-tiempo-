package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInsuficienteException;
import com.bancotiempo.pasaportesaberes.domain.exception.SaldoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Billetera {
    private static final int ESCALA_HORAS = 2;
    private static final int PRECISION_HORAS = 8;

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
        horas = validarHoras(horas);
        if (saldoDisponible.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoDisponible);
        }

        saldoDisponible = saldoDisponible.subtract(horas);
        saldoReservado = saldoReservado.add(horas);
        version++;
    }

    public void liberar(BigDecimal horas) {
        validarBilleteraDeUsuario();
        horas = validarHoras(horas);
        if (saldoReservado.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoReservado);
        }

        saldoReservado = saldoReservado.subtract(horas);
        saldoDisponible = saldoDisponible.add(horas);
        version++;
    }

    public void acreditar(BigDecimal horas) {
        horas = validarHoras(horas);
        BigDecimal nuevoDisponible = saldoDisponible.add(horas);
        validarSaldos(nuevoDisponible, saldoReservado);
        saldoDisponible = nuevoDisponible;
        version++;
    }

    public void debitar(BigDecimal horas) {
        horas = validarHoras(horas);
        if (tipo != TipoBilletera.EMISOR && saldoDisponible.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoDisponible);
        }

        BigDecimal nuevoDisponible = saldoDisponible.subtract(horas);
        validarSaldos(nuevoDisponible, saldoReservado);
        saldoDisponible = nuevoDisponible;
        version++;
    }

    public void debitarReservado(BigDecimal horas) {
        validarBilleteraDeUsuario();
        horas = validarHoras(horas);
        if (saldoReservado.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoReservado);
        }

        saldoReservado = saldoReservado.subtract(horas);
        version++;
    }

    public void transferirDisponibleA(Billetera destino, BigDecimal horas) {
        validarDestino(destino);
        horas = validarHoras(horas);
        validarTransferenciaDisponible(destino);
        if (tipo != TipoBilletera.EMISOR && saldoDisponible.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoDisponible);
        }

        BigDecimal nuevoOrigen = saldoDisponible.subtract(horas);
        BigDecimal nuevoDestino = destino.saldoDisponible.add(horas);
        validarSaldos(nuevoOrigen, saldoReservado);
        destino.validarSaldos(nuevoDestino, destino.saldoReservado);

        saldoDisponible = nuevoOrigen;
        destino.saldoDisponible = nuevoDestino;
        version++;
        destino.version++;
    }

    public void transferirReservadoA(Billetera destino, BigDecimal horas) {
        validarBilleteraDeUsuario();
        validarDestino(destino);
        horas = validarHoras(horas);
        if (destino.tipo != TipoBilletera.USUARIO
                && destino.tipo != TipoBilletera.FONDO) {
            throw new SaldoInvalidoException(
                    "Las horas reservadas solo pueden transferirse a USUARIO o FONDO");
        }
        if (saldoReservado.compareTo(horas) < 0) {
            throw new SaldoInsuficienteException(id, horas, saldoReservado);
        }

        BigDecimal nuevoReservado = saldoReservado.subtract(horas);
        BigDecimal nuevoDestino = destino.saldoDisponible.add(horas);
        validarSaldos(saldoDisponible, nuevoReservado);
        destino.validarSaldos(nuevoDestino, destino.saldoReservado);

        saldoReservado = nuevoReservado;
        destino.saldoDisponible = nuevoDestino;
        version++;
        destino.version++;
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
        validarSaldos(saldoDisponible, saldoReservado);
    }

    private void validarSaldos(BigDecimal disponible, BigDecimal reservado) {
        validarPrecisionSaldo(disponible);
        validarPrecisionSaldo(reservado);
        if (reservado.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInvalidoException("El saldo reservado no puede ser negativo");
        }
        if (tipo != TipoBilletera.EMISOR
                && disponible.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInvalidoException("El saldo disponible no puede ser negativo");
        }
    }

    private void validarDestino(Billetera destino) {
        Objects.requireNonNull(destino, "La billetera destino es obligatoria");
        if (destino == this || (id != null && id.equals(destino.id))) {
            throw new SaldoInvalidoException("El origen y el destino deben ser distintos");
        }
    }

    private void validarTransferenciaDisponible(Billetera destino) {
        boolean permitida =
                (tipo == TipoBilletera.EMISOR && destino.tipo == TipoBilletera.USUARIO)
                        || (tipo == TipoBilletera.USUARIO && destino.tipo == TipoBilletera.FONDO)
                        || (tipo == TipoBilletera.FONDO && destino.tipo == TipoBilletera.USUARIO);
        if (!permitida) {
            throw new SaldoInvalidoException("Tipo de transferencia de saldo disponible inválido");
        }
    }

    private static BigDecimal validarHoras(BigDecimal horas) {
        if (horas == null || horas.signum() <= 0) {
            throw new SaldoInvalidoException("Las horas deben ser mayores que cero");
        }
        return normalizarImporte(horas, "Las horas deben tener como máximo dos decimales");
    }

    private static BigDecimal normalizar(BigDecimal valor) {
        return valor == null
                ? BigDecimal.ZERO.setScale(ESCALA_HORAS)
                : normalizarImporte(valor, "El saldo debe tener como máximo dos decimales");
    }

    private static BigDecimal normalizarImporte(BigDecimal valor, String mensaje) {
        BigDecimal normalizado;
        try {
            normalizado = valor.setScale(ESCALA_HORAS, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new SaldoInvalidoException(mensaje);
        }
        if (normalizado.precision() > PRECISION_HORAS) {
            throw new SaldoInvalidoException("El valor excede la precisión NUMERIC(8,2)");
        }
        return normalizado;
    }

    private static void validarPrecisionSaldo(BigDecimal saldo) {
        if (saldo.precision() > PRECISION_HORAS) {
            throw new SaldoInvalidoException("El saldo excede la precisión NUMERIC(8,2)");
        }
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
