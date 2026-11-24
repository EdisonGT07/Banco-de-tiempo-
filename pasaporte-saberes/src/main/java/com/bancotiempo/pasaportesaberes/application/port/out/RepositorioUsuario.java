package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;

import java.util.Optional;

public interface RepositorioUsuario {
    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existeCorreo(String correo);

    boolean existeDocumentoHmac(String docHmac);

    Persona guardarPersona(Persona persona);

    EntidadAliada guardarEntidad(EntidadAliada entidad);

    Administrador guardarAdministrador(Administrador administrador);
}
