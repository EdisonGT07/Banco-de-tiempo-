package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioEvidencia;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.domain.model.Evidencia;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioEvidenciaJpa implements RepositorioEvidencia {
    private final EvidenciaJpaRepository evidencias;
    private final RepositorioSesion sesiones;

    public RepositorioEvidenciaJpa(
            EvidenciaJpaRepository evidencias,
            RepositorioSesion sesiones
    ) {
        this.evidencias = evidencias;
        this.sesiones = sesiones;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Evidencia> buscarPorSesion(Long sesionId) {
        return evidencias.findBySesionId(sesionId).map(this::aDominio);
    }

    @Override
    public Evidencia guardar(Evidencia evidencia) {
        Objects.requireNonNull(evidencia, "La evidencia es obligatoria");
        EvidenciaJpaEntity entidad;
        if (evidencia.id() == null) {
            entidad = new EvidenciaJpaEntity();
        } else {
            entidad = evidencias.findByIdForUpdate(evidencia.id()).orElseThrow(
                    () -> new IllegalStateException(
                            "No existe la evidencia que se quiere actualizar"));
        }

        entidad.setSesionId(evidencia.sesion().id());
        entidad.setTokenHash(evidencia.tokenHash());
        entidad.setCodigoCortoHash(evidencia.codigoCortoHash());
        entidad.setExpiraEn(evidencia.expiraEn());
        entidad.setUsadoEn(evidencia.usadoEn());
        entidad.setIntentosFallidos(evidencia.intentosFallidos());
        entidad.setFotoUrl(evidencia.fotoUrl());
        entidad.setLatitud(evidencia.latitud());
        entidad.setLongitud(evidencia.longitud());
        entidad.setMarcaServidor(evidencia.marcaServidor());
        return aDominio(evidencias.saveAndFlush(entidad));
    }

    @Override
    public Optional<Evidencia> consumirSiUsable(
            Long sesionId,
            String hashPresentado,
            OffsetDateTime ahora
    ) {
        int actualizadas = evidencias.consumirSiUsable(sesionId, hashPresentado, ahora);
        if (actualizadas == 0) {
            return Optional.empty();
        }
        return evidencias.findBySesionId(sesionId).map(this::aDominio);
    }

    @Override
    public boolean registrarIntentoFallidoSiUsable(Long sesionId, OffsetDateTime ahora) {
        return evidencias.registrarIntentoFallidoSiUsable(sesionId, ahora) == 1;
    }

    private Evidencia aDominio(EvidenciaJpaEntity entidad) {
        Sesion sesion = sesiones.buscarPorId(entidad.getSesionId()).orElseThrow(
                () -> new IllegalStateException(
                        "La evidencia referencia una sesión inexistente"));
        return new Evidencia(
                entidad.getId(),
                sesion,
                entidad.getTokenHash(),
                entidad.getCodigoCortoHash(),
                entidad.getExpiraEn(),
                entidad.getUsadoEn(),
                entidad.getIntentosFallidos(),
                entidad.getFotoUrl(),
                entidad.getLatitud(),
                entidad.getLongitud(),
                entidad.getMarcaServidor()
        );
    }
}
