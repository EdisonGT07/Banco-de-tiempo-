package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.OffsetDateTime;

public abstract class Usuario {
    private Long id;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private OffsetDateTime habeasDataEn;

    protected Usuario() {
    }

    protected Usuario(Long id, String nombre, String correo, String contrasenaHash,
                      OffsetDateTime habeasDataEn) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.habeasDataEn = habeasDataEn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public OffsetDateTime getHabeasDataEn() {
        return habeasDataEn;
    }

    public void setHabeasDataEn(OffsetDateTime habeasDataEn) {
        this.habeasDataEn = habeasDataEn;
    }
}
