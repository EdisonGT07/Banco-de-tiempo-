package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioCredencial;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioHabilidad;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioCredencialJpa implements RepositorioCredencial {
    private final CredencialJpaRepository credenciales;
    private final RepositorioUsuario usuarios;
    private final RepositorioHabilidad habilidades;

    public RepositorioCredencialJpa(
            CredencialJpaRepository credenciales,
            RepositorioUsuario usuarios,
            RepositorioHabilidad habilidades
    ) {
        this.credenciales = credenciales;
        this.usuarios = usuarios;
        this.habilidades = habilidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Credencial> buscarPorId(Long id) {
        return credenciales.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Credencial> buscarPorCodigo(String codigo) {
        return credenciales.findByCodigoVerificacion(codigo).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Credencial> listarPorPersona(Long personaId) {
        return credenciales.findAllByPersonaId(personaId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Credencial> buscarPorPersonaYHabilidad(
            Long personaId,
            Long habilidadId
    ) {
        return credenciales.findByPersonaIdAndHabilidadId(personaId, habilidadId)
                .map(this::aDominio);
    }

    @Override
    public Credencial guardar(Credencial credencial) {
        Objects.requireNonNull(credencial, "La credencial es obligatoria");
        CredencialJpaEntity entidad = credencial.id() == null
                ? new CredencialJpaEntity()
                : credenciales.findById(credencial.id()).orElseThrow(
                () -> new IllegalStateException(
                        "No existe la credencial que se quiere actualizar"));
        entidad.setPersonaId(credencial.persona().getId());
        entidad.setHabilidadId(credencial.habilidad().id());
        entidad.setHorasEnsenadas(credencial.horasEnsenadas());
        entidad.setRatingPromedio(credencial.ratingPromedio());
        entidad.setCodigoVerificacion(credencial.codigoVerificacion());
        entidad.setFirma(credencial.firma());
        entidad.setActualizadaEn(credencial.actualizadaEn());
        entidad.setNumeroVerificaciones(credencial.numeroVerificaciones());
        return aDominio(credenciales.saveAndFlush(entidad));
    }

    private Credencial aDominio(CredencialJpaEntity entidad) {
        Usuario usuario = usuarios.buscarPorId(entidad.getPersonaId()).orElseThrow(
                () -> new IllegalStateException(
                        "La credencial referencia una persona inexistente"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException(
                    "El propietario de una credencial debe ser una persona");
        }
        Habilidad habilidad = habilidades.buscarPorId(entidad.getHabilidadId())
                .orElseThrow(() -> new IllegalStateException(
                        "La credencial referencia una habilidad inexistente"));
        return new Credencial(
                entidad.getId(),
                persona,
                habilidad,
                entidad.getHorasEnsenadas(),
                entidad.getRatingPromedio(),
                entidad.getCodigoVerificacion(),
                entidad.getFirma(),
                entidad.getActualizadaEn(),
                entidad.getNumeroVerificaciones()
        );
    }
}
