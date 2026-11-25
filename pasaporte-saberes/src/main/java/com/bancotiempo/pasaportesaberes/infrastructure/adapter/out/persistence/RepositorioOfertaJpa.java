package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioHabilidad;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioOferta;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioOfertaJpa implements RepositorioOferta {
    private final OfertaJpaRepository ofertas;
    private final RepositorioUsuario usuarios;
    private final RepositorioHabilidad habilidades;

    public RepositorioOfertaJpa(
            OfertaJpaRepository ofertas,
            RepositorioUsuario usuarios,
            RepositorioHabilidad habilidades
    ) {
        this.ofertas = ofertas;
        this.usuarios = usuarios;
        this.habilidades = habilidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Oferta> buscarPorId(Long id) {
        return ofertas.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Oferta> listarPorTutor(Long tutorId) {
        return ofertas.findAllByTutorId(tutorId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Oferta guardar(Oferta oferta) {
        Objects.requireNonNull(oferta, "La oferta es obligatoria");
        OfertaJpaEntity entidad;
        if (oferta.id() == null) {
            entidad = new OfertaJpaEntity();
        } else {
            entidad = ofertas.findById(oferta.id()).orElseThrow(
                    () -> new IllegalStateException("No existe la oferta que se quiere actualizar"));
        }

        entidad.setTutorId(oferta.tutor().getId());
        entidad.setHabilidadId(oferta.habilidad().id());
        entidad.setDescripcion(oferta.descripcion());
        entidad.setDuracionHoras(oferta.duracionHoras());
        entidad.setModalidad(oferta.modalidad());
        entidad.setBarrio(oferta.barrio());
        entidad.setDestinoHoras(oferta.destinoHoras());
        entidad.setReceptorRegaloId(
                oferta.receptorRegalo() == null ? null : oferta.receptorRegalo().getId());
        entidad.setActiva(oferta.activa());
        entidad.setCreadaEn(oferta.creadaEn());
        return aDominio(ofertas.saveAndFlush(entidad));
    }

    private Oferta aDominio(OfertaJpaEntity entidad) {
        Usuario tutor = usuarios.buscarPorId(entidad.getTutorId()).orElseThrow(
                () -> new IllegalStateException("La oferta referencia un tutor inexistente"));
        if (!(tutor instanceof Persona personaTutor)) {
            throw new IllegalStateException("El tutor de una oferta debe ser una persona");
        }
        Habilidad habilidad = habilidades.buscarPorId(entidad.getHabilidadId()).orElseThrow(
                () -> new IllegalStateException("La oferta referencia una habilidad inexistente"));
        Persona receptor = entidad.getReceptorRegaloId() == null
                ? null
                : buscarPersona(entidad.getReceptorRegaloId());
        return new Oferta(
                entidad.getId(),
                personaTutor,
                habilidad,
                entidad.getDescripcion(),
                entidad.getDuracionHoras(),
                entidad.getModalidad(),
                entidad.getBarrio(),
                entidad.getDestinoHoras(),
                receptor,
                entidad.isActiva(),
                entidad.getCreadaEn()
        );
    }

    private Persona buscarPersona(Long id) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(
                () -> new IllegalStateException("La oferta referencia un receptor inexistente"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException("El receptor de un regalo debe ser una persona");
        }
        return persona;
    }
}
