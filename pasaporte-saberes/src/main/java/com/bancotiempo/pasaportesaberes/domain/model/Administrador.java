package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.UsuarioInvalidoException;

import java.time.OffsetDateTime;

public class Administrador extends Usuario {
    private Short nivelAcceso = 1;

    public Administrador() {
    }

    public Administrador(
            Long id,
            String nombre,
            String correo,
            String contrasenaHash,
            OffsetDateTime habeasDataEn,
            Short nivelAcceso
    ) {
        super(id, nombre, correo, contrasenaHash, habeasDataEn);
        setNivelAcceso(nivelAcceso);
    }

    public Short getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(Short nivelAcceso) {
        if (nivelAcceso == null) {
            throw new UsuarioInvalidoException("El nivel de acceso es obligatorio");
        }
        this.nivelAcceso = nivelAcceso;
    }

    @Override
    public void validarDatosObligatorios() {
        super.validarDatosObligatorios();
        if (nivelAcceso == null) {
            throw new UsuarioInvalidoException("El nivel de acceso es obligatorio");
        }
    }
}
