package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioUsuarioJpa implements RepositorioUsuario {
    private static final String TIPO_PERSONA = "PERSONA";
    private static final String TIPO_ENTIDAD = "ENTIDAD";
    private static final String TIPO_ADMIN = "ADMIN";

    private final UsuarioJpaRepository usuarios;
    private final PersonaJpaRepository personas;
    private final EntidadAliadaJpaRepository entidades;
    private final AdministradorJpaRepository administradores;
    private final RangoJpaRepository rangos;

    public RepositorioUsuarioJpa(
            UsuarioJpaRepository usuarios,
            PersonaJpaRepository personas,
            EntidadAliadaJpaRepository entidades,
            AdministradorJpaRepository administradores,
            RangoJpaRepository rangos
    ) {
        this.usuarios = usuarios;
        this.personas = personas;
        this.entidades = entidades;
        this.administradores = administradores;
        this.rangos = rangos;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarios.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarios.findByCorreo(correo).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        return usuarios.existsByCorreo(correo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocumentoHmac(String docHmac) {
        return personas.existsByDocHmac(docHmac);
    }

    @Override
    public Persona guardarPersona(Persona persona) {
        UsuarioJpaEntity usuario = prepararUsuario(persona, TIPO_PERSONA);
        usuario = usuarios.saveAndFlush(usuario);

        PersonaJpaEntity entidad = persona.getId() == null
                ? new PersonaJpaEntity()
                : personas.findById(persona.getId()).orElseThrow(
                () -> new IllegalStateException("Falta la fila persona del usuario existente"));
        copiarPersona(persona, entidad);
        entidad.setUsuarioId(usuario.getId());
        entidad.setTipo(TIPO_PERSONA);
        entidad = personas.saveAndFlush(entidad);
        persona.setId(usuario.getId());
        return aDominio(usuario, entidad);
    }

    @Override
    public EntidadAliada guardarEntidad(EntidadAliada entidadAliada) {
        UsuarioJpaEntity usuario = prepararUsuario(entidadAliada, TIPO_ENTIDAD);
        usuario = usuarios.saveAndFlush(usuario);

        EntidadAliadaJpaEntity entidad = entidadAliada.getId() == null
                ? new EntidadAliadaJpaEntity()
                : entidades.findById(entidadAliada.getId()).orElseThrow(
                () -> new IllegalStateException(
                        "Falta la fila entidad_aliada del usuario existente"));
        entidad.setUsuarioId(usuario.getId());
        entidad.setTipo(TIPO_ENTIDAD);
        entidad.setTipoEntidad(entidadAliada.getTipoEntidad());
        entidad.setCodigoInstitucional(entidadAliada.getCodigoInstitucional());
        entidad = entidades.saveAndFlush(entidad);
        entidadAliada.setId(usuario.getId());
        return aDominio(usuario, entidad);
    }

    @Override
    public Administrador guardarAdministrador(Administrador administrador) {
        UsuarioJpaEntity usuario = prepararUsuario(administrador, TIPO_ADMIN);
        usuario = usuarios.saveAndFlush(usuario);

        AdministradorJpaEntity entidad = administrador.getId() == null
                ? new AdministradorJpaEntity()
                : administradores.findById(administrador.getId()).orElseThrow(
                () -> new IllegalStateException(
                        "Falta la fila administrador del usuario existente"));
        entidad.setUsuarioId(usuario.getId());
        entidad.setTipo(TIPO_ADMIN);
        entidad.setNivelAcceso(administrador.getNivelAcceso());
        entidad = administradores.saveAndFlush(entidad);
        administrador.setId(usuario.getId());
        return aDominio(usuario, entidad);
    }

    private UsuarioJpaEntity prepararUsuario(Usuario dominio, String tipoEsperado) {
        dominio.validarDatosObligatorios();
        UsuarioJpaEntity entidad;
        if (dominio.getId() == null) {
            entidad = new UsuarioJpaEntity();
        } else {
            entidad = usuarios.findById(dominio.getId()).orElseThrow(
                    () -> new IllegalStateException("No existe el usuario que se quiere actualizar"));
            if (!tipoEsperado.equals(entidad.getTipo())) {
                throw new IllegalStateException(
                        "El subtipo de usuario persistido no coincide con el recibido");
            }
        }
        entidad.setTipo(tipoEsperado);
        entidad.setNombre(dominio.getNombre());
        entidad.setCorreo(dominio.getCorreo());
        entidad.setContrasenaHash(dominio.getContrasenaHash());
        entidad.setHabeasDataEn(dominio.getHabeasDataEn());
        return entidad;
    }

    private Usuario aDominio(UsuarioJpaEntity usuario) {
        return switch (usuario.getTipo()) {
            case TIPO_PERSONA -> {
                PersonaJpaEntity persona = personas.findById(usuario.getId()).orElseThrow(
                        () -> new IllegalStateException(
                                "El usuario PERSONA no tiene su fila de especialización"));
                yield aDominio(usuario, persona);
            }
            case TIPO_ENTIDAD -> {
                EntidadAliadaJpaEntity entidad = entidades.findById(usuario.getId()).orElseThrow(
                        () -> new IllegalStateException(
                                "El usuario ENTIDAD no tiene su fila de especialización"));
                yield aDominio(usuario, entidad);
            }
            case TIPO_ADMIN -> {
                AdministradorJpaEntity administrador =
                        administradores.findById(usuario.getId()).orElseThrow(
                                () -> new IllegalStateException(
                                        "El usuario ADMIN no tiene su fila de especialización"));
                yield aDominio(usuario, administrador);
            }
            default -> throw new IllegalStateException(
                    "Tipo de usuario persistido no soportado: " + usuario.getTipo());
        };
    }

    private Persona aDominio(UsuarioJpaEntity usuario, PersonaJpaEntity personaEntidad) {
        Persona persona = new Persona();
        copiarUsuario(usuario, persona);
        persona.setDocHmac(personaEntidad.getDocHmac());
        persona.registrarFechaNacimiento(
                personaEntidad.getFechaNacimiento(),
                LocalDate.now(ZoneOffset.UTC)
        );
        persona.setZona(personaEntidad.getZona());
        persona.setEstrato(personaEntidad.getEstrato());
        if (personaEntidad.getEstado() == Persona.Estado.SUSPENDIDO) {
            persona.suspender();
        }
        persona.setBloqueadoHasta(personaEntidad.getBloqueadoHasta());
        persona.setRangoEnsenanza(aRango(personaEntidad.getRangoEnsenanzaId()));
        persona.setRangoAprendizaje(aRango(personaEntidad.getRangoAprendizajeId()));
        return persona;
    }

    private EntidadAliada aDominio(
            UsuarioJpaEntity usuario,
            EntidadAliadaJpaEntity entidad
    ) {
        return new EntidadAliada(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                usuario.getHabeasDataEn(),
                entidad.getTipoEntidad(),
                entidad.getCodigoInstitucional()
        );
    }

    private Administrador aDominio(
            UsuarioJpaEntity usuario,
            AdministradorJpaEntity administrador
    ) {
        return new Administrador(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                usuario.getHabeasDataEn(),
                administrador.getNivelAcceso()
        );
    }

    private void copiarPersona(Persona persona, PersonaJpaEntity entidad) {
        entidad.setDocHmac(persona.getDocHmac());
        entidad.setFechaNacimiento(persona.getFechaNacimiento());
        entidad.setZona(persona.getZona());
        entidad.setEstrato(persona.getEstrato());
        entidad.setEstado(persona.getEstado());
        entidad.setBloqueadoHasta(persona.getBloqueadoHasta());
        entidad.setRangoEnsenanzaId(idRango(persona.getRangoEnsenanza()));
        entidad.setRangoAprendizajeId(idRango(persona.getRangoAprendizaje()));
    }

    private Rango aRango(Long rangoId) {
        if (rangoId == null) {
            return null;
        }
        RangoJpaEntity rango = rangos.findById(rangoId).orElseThrow(
                () -> new IllegalStateException("La persona referencia un rango inexistente"));
        return RepositorioRangoJpa.aDominio(rango);
    }

    private static Long idRango(Rango rango) {
        return rango == null ? null : rango.id();
    }

    private static void copiarUsuario(UsuarioJpaEntity entidad, Usuario usuario) {
        usuario.setId(entidad.getId());
        usuario.setNombre(entidad.getNombre());
        usuario.setCorreo(entidad.getCorreo());
        usuario.setContrasenaHash(entidad.getContrasenaHash());
        usuario.setHabeasDataEn(entidad.getHabeasDataEn());
    }
}
