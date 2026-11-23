package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class Persona extends Usuario {
    public enum Zona {
        URBANA,
        RURAL
    }

    public enum Estado {
        ACTIVO,
        SUSPENDIDO
    }

    private String docHmac;
    private LocalDate fechaNacimiento;
    private Zona zona;
    private Short estrato;
    private Estado estado = Estado.ACTIVO;
    private OffsetDateTime bloqueadoHasta;
    private Rango rangoEnsenanza;
    private Rango rangoAprendizaje;

    public String getDocHmac() {
        return docHmac;
    }

    public void setDocHmac(String docHmac) {
        this.docHmac = docHmac;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Short getEstrato() {
        return estrato;
    }

    public void setEstrato(Short estrato) {
        this.estrato = estrato;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public OffsetDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(OffsetDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Rango getRangoEnsenanza() {
        return rangoEnsenanza;
    }

    public void setRangoEnsenanza(Rango rangoEnsenanza) {
        this.rangoEnsenanza = rangoEnsenanza;
    }

    public Rango getRangoAprendizaje() {
        return rangoAprendizaje;
    }

    public void setRangoAprendizaje(Rango rangoAprendizaje) {
        this.rangoAprendizaje = rangoAprendizaje;
    }
}
