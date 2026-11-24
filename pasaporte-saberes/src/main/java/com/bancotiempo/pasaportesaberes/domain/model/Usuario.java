package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.UsuarioInvalidoException;

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
        setNombre(nombre);
        setCorreo(correo);
        setContrasenaHash(contrasenaHash);
        setHabeasDataEn(habeasDataEn);
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
        if (nombre == null || nombre.isBlank() || nombre.length() > 120) {
            throw new UsuarioInvalidoException(
                    "El nombre es obligatorio y no puede superar 120 caracteres");
        }
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.isBlank() || correo.length() > 160) {
            throw new UsuarioInvalidoException(
                    "El correo es obligatorio y no puede superar 160 caracteres");
        }
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        if (contrasenaHash == null || contrasenaHash.isBlank()
                || contrasenaHash.length() > 100) {
            throw new UsuarioInvalidoException(
                    "El hash de contraseña es obligatorio y no puede superar 100 caracteres");
        }
        this.contrasenaHash = contrasenaHash;
    }

    public OffsetDateTime getHabeasDataEn() {
        return habeasDataEn;
    }

    public void setHabeasDataEn(OffsetDateTime habeasDataEn) {
        if (habeasDataEn == null) {
            throw new UsuarioInvalidoException(
                    "La aceptación de Habeas Data es obligatoria");
        }
        this.habeasDataEn = habeasDataEn;
    }

    public void validarDatosObligatorios() {
        validarTexto(nombre, 120, "El nombre");
        validarTexto(correo, 160, "El correo");
        validarTexto(contrasenaHash, 100, "El hash de contraseña");
        if (habeasDataEn == null) {
            throw new UsuarioInvalidoException(
                    "La aceptación de Habeas Data es obligatoria");
        }
    }

    private static void validarTexto(String valor, int longitudMaxima, String campo) {
        if (valor == null || valor.isBlank() || valor.length() > longitudMaxima) {
            throw new UsuarioInvalidoException(
                    campo + " es obligatorio y no puede superar " + longitudMaxima
                            + " caracteres");
        }
    }
}
