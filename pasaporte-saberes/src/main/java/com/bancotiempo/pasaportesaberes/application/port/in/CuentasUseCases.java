package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Persona;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface CuentasUseCases {
    Perfil registrarUsuario(RegistroUsuario solicitud);

    SesionAutenticada iniciarSesion(CredencialesInicioSesion solicitud);

    Perfil obtenerMiPerfil(Long usuarioId);

    record RegistroUsuario(
            String nombre,
            String correo,
            String contrasena,
            String documento,
            LocalDate fechaNacimiento,
            Persona.Zona zona,
            Short estrato,
            OffsetDateTime aceptaHabeasDataEn
    ) {
    }

    record CredencialesInicioSesion(String correo, String contrasena) {
    }

    record Perfil(
            Long id,
            String nombre,
            String correo,
            LocalDate fechaNacimiento,
            Persona.Zona zona,
            Short estrato,
            Persona.Estado estado
    ) {
    }

    record SesionAutenticada(String token, Perfil perfil) {
    }
}
