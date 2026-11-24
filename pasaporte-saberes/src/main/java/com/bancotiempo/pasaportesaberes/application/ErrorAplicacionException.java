package com.bancotiempo.pasaportesaberes.application;

public class ErrorAplicacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public enum Codigo {
        NO_ENCONTRADO,
        CONFLICTO,
        NO_AUTORIZADO,
        VALIDACION
    }

    private final Codigo codigo;

    public ErrorAplicacionException(Codigo codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public Codigo getCodigo() {
        return codigo;
    }
}
