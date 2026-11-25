package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.DesbloqueadorPersona;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DesbloqueadorPersonaJpa implements DesbloqueadorPersona {
    private final RepositorioUsuario usuarios;

    public DesbloqueadorPersonaJpa(RepositorioUsuario usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public Persona desbloquear(Long personaId) {
        Usuario usuario = usuarios.buscarPorId(personaId).orElseThrow(
                () -> new IllegalStateException("No existe la persona que se quiere desbloquear"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException("Solo una persona puede ser desbloqueada");
        }
        persona.activar();
        persona.setBloqueadoHasta(null);
        return usuarios.guardarPersona(persona);
    }
}
