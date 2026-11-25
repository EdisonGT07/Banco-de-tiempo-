package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioHabilidad;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioQuererAprender;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprenderId;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioQuererAprenderJpa implements RepositorioQuererAprender {
    private final QuererAprenderJpaRepository solicitudes;
    private final RepositorioUsuario usuarios;
    private final RepositorioHabilidad habilidades;

    public RepositorioQuererAprenderJpa(
            QuererAprenderJpaRepository solicitudes,
            RepositorioUsuario usuarios,
            RepositorioHabilidad habilidades
    ) {
        this.solicitudes = solicitudes;
        this.usuarios = usuarios;
        this.habilidades = habilidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuererAprender> buscarPorId(QuererAprenderId id) {
        Objects.requireNonNull(id, "El id de la solicitud es obligatorio");
        QuererAprenderJpaId idJpa = new QuererAprenderJpaId(
                id.personaId(), id.habilidadId());
        return solicitudes.findById(idJpa).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuererAprender> listarPorPersona(Long personaId) {
        return solicitudes.findAllByIdPersonaIdOrderByCreadaEn(personaId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public QuererAprender guardar(QuererAprender quererAprender) {
        Objects.requireNonNull(quererAprender, "La solicitud de aprendizaje es obligatoria");
        QuererAprenderJpaEntity entidad = new QuererAprenderJpaEntity();
        entidad.setId(new QuererAprenderJpaId(
                quererAprender.id().personaId(),
                quererAprender.id().habilidadId()
        ));
        entidad.setCreadaEn(quererAprender.creadaEn());
        return aDominio(solicitudes.saveAndFlush(entidad));
    }

    @Override
    public void eliminar(QuererAprenderId id) {
        Objects.requireNonNull(id, "El id de la solicitud es obligatorio");
        solicitudes.deleteById(new QuererAprenderJpaId(
                id.personaId(), id.habilidadId()));
    }

    private QuererAprender aDominio(QuererAprenderJpaEntity entidad) {
        Long personaId = entidad.getId().getPersonaId();
        Long habilidadId = entidad.getId().getHabilidadId();
        Usuario usuario = usuarios.buscarPorId(personaId).orElseThrow(
                () -> new IllegalStateException(
                        "La solicitud referencia una persona inexistente"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException(
                    "El propietario de una solicitud debe ser una persona");
        }
        Habilidad habilidad = habilidades.buscarPorId(habilidadId).orElseThrow(
                () -> new IllegalStateException(
                        "La solicitud referencia una habilidad inexistente"));
        return new QuererAprender(
                new QuererAprenderId(personaId, habilidadId),
                persona,
                habilidad,
                entidad.getCreadaEn()
        );
    }
}
