package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioBilleteraJpa implements RepositorioBilletera {
    private final BilleteraJpaRepository billeteras;
    private final RepositorioUsuario usuarios;

    public RepositorioBilleteraJpa(
            BilleteraJpaRepository billeteras,
            RepositorioUsuario usuarios
    ) {
        this.billeteras = billeteras;
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> buscarPorId(Long id) {
        return billeteras.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> buscarPorPersona(Long personaId) {
        return billeteras.findByPersonaId(personaId).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> buscarSistema(Billetera.TipoBilletera tipo) {
        if (tipo == Billetera.TipoBilletera.USUARIO) {
            return Optional.empty();
        }
        return billeteras.findByTipo(tipo).map(this::aDominio);
    }

    @Override
    public List<Billetera> bloquearPorIds(List<Long> ids) {
        Objects.requireNonNull(ids, "Los ids de billetera son obligatorios");
        List<Long> ordenados = ids.stream()
                .map(id -> Objects.requireNonNull(id, "El id de billetera es obligatorio"))
                .distinct()
                .sorted()
                .toList();
        if (ordenados.isEmpty()) {
            return List.of();
        }

        List<BilleteraJpaEntity> bloqueadas =
                billeteras.findByIdInOrderByIdAsc(ordenados);
        if (bloqueadas.size() != ordenados.size()) {
            throw new IllegalStateException(
                    "No se encontraron todas las billeteras solicitadas para bloqueo");
        }
        return bloqueadas.stream().map(this::aDominio).toList();
    }

    @Override
    public Billetera guardar(Billetera billetera) {
        Objects.requireNonNull(billetera, "La billetera es obligatoria");
        BilleteraJpaEntity entidad;
        if (billetera.getId() == null) {
            if (billetera.getVersion() != 0) {
                throw new OptimisticLockException(
                        "Una billetera nueva debe comenzar en la versión cero");
            }
            entidad = new BilleteraJpaEntity();
        } else {
            entidad = billeteras.findById(billetera.getId()).orElseThrow(
                    () -> new IllegalStateException("No existe la billetera que se quiere guardar"));
            if (entidad.getTipo() != billetera.getTipo()
                    || !Objects.equals(entidad.getPersonaId(), idPersona(billetera))) {
                throw new IllegalStateException(
                        "No se permite cambiar la identidad de una billetera persistida");
            }
            long versionPersistida = version(entidad);
            if (billetera.getVersion() != versionPersistida
                    && billetera.getVersion() != versionPersistida + 1) {
                throw new OptimisticLockException(
                        "La versión de la billetera no corresponde al estado persistido");
            }
        }

        entidad.setTipo(billetera.getTipo());
        entidad.setPersonaId(idPersona(billetera));
        entidad.setSaldoDisponible(billetera.getSaldoDisponible());
        entidad.setSaldoReservado(billetera.getSaldoReservado());
        return aDominio(billeteras.saveAndFlush(entidad));
    }

    private Billetera aDominio(BilleteraJpaEntity entidad) {
        Persona persona = null;
        if (entidad.getPersonaId() != null) {
            Usuario usuario = usuarios.buscarPorId(entidad.getPersonaId()).orElseThrow(
                    () -> new IllegalStateException(
                            "La billetera referencia una persona inexistente"));
            if (!(usuario instanceof Persona personaEncontrada)) {
                throw new IllegalStateException(
                        "La billetera de usuario referencia una cuenta que no es persona");
            }
            persona = personaEncontrada;
        }
        return new Billetera(
                entidad.getId(),
                entidad.getTipo(),
                persona,
                entidad.getSaldoDisponible(),
                entidad.getSaldoReservado(),
                version(entidad)
        );
    }

    private static Long idPersona(Billetera billetera) {
        return billetera.getPersona() == null ? null : billetera.getPersona().getId();
    }

    private static long version(BilleteraJpaEntity entidad) {
        return entidad.getVersion() == null ? 0 : entidad.getVersion();
    }
}
