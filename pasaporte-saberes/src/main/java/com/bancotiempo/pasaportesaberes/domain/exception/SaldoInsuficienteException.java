package com.bancotiempo.pasaportesaberes.domain.exception;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends DomainException {
    public SaldoInsuficienteException(Long billeteraId, BigDecimal solicitado,
                                      BigDecimal disponible) {
        super("Saldo insuficiente en la billetera " + billeteraId
                + ": solicitado " + solicitado + ", disponible " + disponible);
    }
}
