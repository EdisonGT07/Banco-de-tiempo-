package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.CuentasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorHmacDocumento;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorToken;
import com.bancotiempo.pasaportesaberes.application.port.out.HasheadorContrasena;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioTransaccion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;

import java.math.BigDecimal;
import java.util.Objects;

public final class CuentasService implements CuentasUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioBilletera billeteras;
    private final RepositorioTransaccion transacciones;
    private final HasheadorContrasena contrasenas;
    private final GeneradorHmacDocumento documentos;
    private final GeneradorToken tokens;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;
    private final BigDecimal bonoInicial;

    public CuentasService(
            RepositorioUsuario usuarios,
            RepositorioBilletera billeteras,
            RepositorioTransaccion transacciones,
            HasheadorContrasena contrasenas,
            GeneradorHmacDocumento documentos,
            GeneradorToken tokens,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            BigDecimal bonoInicial
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.billeteras = Objects.requireNonNull(billeteras);
        this.transacciones = Objects.requireNonNull(transacciones);
        this.contrasenas = Objects.requireNonNull(contrasenas);
        this.documentos = Objects.requireNonNull(documentos);
        this.tokens = Objects.requireNonNull(tokens);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
        if (bonoInicial == null || bonoInicial.signum() <= 0) {
            throw new IllegalArgumentException("El bono inicial debe ser mayor que cero");
        }
        this.bonoInicial = bonoInicial;
    }

    @Override
    public Perfil registrarUsuario(RegistroUsuario solicitud) {
        validarRegistro(solicitud);
        return unidadDeTrabajo.ejecutar(() -> {
            if (usuarios.existeCorreo(solicitud.correo())) {
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "Ya existe una cuenta con ese correo");
            }
            String docHmac = documentos.generar(solicitud.documento());
            if (usuarios.existeDocumentoHmac(docHmac)) {
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "Ya existe una cuenta con ese documento");
            }

            Persona persona = new Persona();
            persona.setNombre(solicitud.nombre());
            persona.setCorreo(solicitud.correo());
            persona.setContrasenaHash(contrasenas.hashear(solicitud.contrasena()));
            persona.setHabeasDataEn(solicitud.aceptaHabeasDataEn());
            persona.setDocHmac(docHmac);
            persona.registrarFechaNacimiento(solicitud.fechaNacimiento(), reloj.hoy());
            persona.setZona(solicitud.zona());
            persona.setEstrato(solicitud.estrato());
            persona.validarDatosObligatorios();
            persona.validarMayorDeEdad(reloj.hoy());

            Persona guardada = usuarios.guardarPersona(persona);
            Billetera wallet = billeteras.guardar(new Billetera(
                    null,
                    Billetera.TipoBilletera.USUARIO,
                    guardada,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0
            ));
            Billetera emisor = billeteras.buscarSistema(Billetera.TipoBilletera.EMISOR)
                    .orElseThrow(() -> error(
                            ErrorAplicacionException.Codigo.NO_ENCONTRADO,
                            "No está configurada la billetera EMISOR"));
            Transaccion bono = new Transaccion(
                    null,
                    Transaccion.TipoTransaccion.BONO,
                    bonoInicial,
                    emisor,
                    wallet,
                    null,
                    Transaccion.claveBono(guardada.getId()),
                    reloj.ahora()
            );
            bono.aplicar();
            billeteras.guardar(emisor);
            billeteras.guardar(wallet);
            transacciones.guardar(bono);
            return perfil(guardada);
        });
    }

    @Override
    public SesionAutenticada iniciarSesion(CredencialesInicioSesion solicitud) {
        if (solicitud == null || solicitud.correo() == null
                || solicitud.contrasena() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El correo y la contraseña son obligatorios");
        }
        Usuario usuario = usuarios.buscarPorCorreo(solicitud.correo())
                .orElseThrow(() -> error(
                        ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                        "Correo o contraseña incorrectos"));
        if (!contrasenas.coincide(solicitud.contrasena(), usuario.getContrasenaHash())) {
            throw error(ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                    "Correo o contraseña incorrectos");
        }
        if (!(usuario instanceof Persona persona)) {
            throw error(ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                    "Este acceso solo está habilitado para personas");
        }
        return new SesionAutenticada(tokens.generarTokenAcceso(usuario.getId()), perfil(persona));
    }

    @Override
    public Perfil obtenerMiPerfil(Long usuarioId) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> error(
                        ErrorAplicacionException.Codigo.NO_ENCONTRADO,
                        "No se encontró el usuario"));
        if (!(usuario instanceof Persona persona)) {
            throw error(ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                    "El perfil personal solo está disponible para personas");
        }
        return perfil(persona);
    }

    private static void validarRegistro(RegistroUsuario solicitud) {
        if (solicitud == null || solicitud.nombre() == null
                || solicitud.correo() == null || solicitud.contrasena() == null
                || solicitud.documento() == null || solicitud.fechaNacimiento() == null
                || solicitud.aceptaHabeasDataEn() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Faltan datos obligatorios para registrar la cuenta");
        }
        if (solicitud.nombre().isBlank() || solicitud.correo().isBlank()
                || solicitud.contrasena().isBlank() || solicitud.documento().isBlank()) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Los datos de registro no pueden estar vacíos");
        }
    }

    private static Perfil perfil(Persona persona) {
        return new Perfil(
                persona.getId(),
                persona.getNombre(),
                persona.getCorreo(),
                persona.getFechaNacimiento(),
                persona.getZona(),
                persona.getEstrato(),
                persona.getEstado()
        );
    }

    private static ErrorAplicacionException error(
            ErrorAplicacionException.Codigo codigo,
            String mensaje
    ) {
        return new ErrorAplicacionException(codigo, mensaje);
    }
}
