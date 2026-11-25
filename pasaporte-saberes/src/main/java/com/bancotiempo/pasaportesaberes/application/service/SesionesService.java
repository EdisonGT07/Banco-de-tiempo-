package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.SesionesUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.AlmacenEvidencias;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorToken;
import com.bancotiempo.pasaportesaberes.application.port.out.NotificadorTope;
import com.bancotiempo.pasaportesaberes.application.port.out.PoliticaTope;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioEvidencia;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioOferta;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioReporte;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioTransaccion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Evidencia;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

public final class SesionesService implements SesionesUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioOferta ofertas;
    private final RepositorioSesion sesiones;
    private final RepositorioBilletera billeteras;
    private final RepositorioTransaccion transacciones;
    private final RepositorioEvidencia evidencias;
    private final RepositorioReporte reportes;
    private final GeneradorToken generadorToken;
    private final AlmacenEvidencias almacenEvidencias;
    private final PoliticaTope politicaTope;
    private final NotificadorTope notificadorTope;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;
    private final PasaporteUseCases pasaporte;
    private final Duration vigenciaHandshake;

    public SesionesService(
            RepositorioUsuario usuarios,
            RepositorioOferta ofertas,
            RepositorioSesion sesiones,
            RepositorioBilletera billeteras,
            RepositorioTransaccion transacciones,
            RepositorioEvidencia evidencias,
            RepositorioReporte reportes,
            GeneradorToken generadorToken,
            AlmacenEvidencias almacenEvidencias,
            PoliticaTope politicaTope,
            NotificadorTope notificadorTope,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            PasaporteUseCases pasaporte,
            Duration vigenciaHandshake
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.ofertas = Objects.requireNonNull(ofertas);
        this.sesiones = Objects.requireNonNull(sesiones);
        this.billeteras = Objects.requireNonNull(billeteras);
        this.transacciones = Objects.requireNonNull(transacciones);
        this.evidencias = Objects.requireNonNull(evidencias);
        this.reportes = Objects.requireNonNull(reportes);
        this.generadorToken = Objects.requireNonNull(generadorToken);
        this.almacenEvidencias = Objects.requireNonNull(almacenEvidencias);
        this.politicaTope = Objects.requireNonNull(politicaTope);
        this.notificadorTope = Objects.requireNonNull(notificadorTope);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
        this.pasaporte = Objects.requireNonNull(pasaporte);
        if (vigenciaHandshake == null || vigenciaHandshake.isZero()
                || vigenciaHandshake.isNegative()) {
            throw new IllegalArgumentException("La vigencia del handshake debe ser positiva");
        }
        this.vigenciaHandshake = vigenciaHandshake;
    }

    @Override
    public Sesion inscribirseEnSesion(Long aprendizId, Long sesionId) {
        return unidadDeTrabajo.ejecutar(() -> {
            Sesion sesion = buscarSesion(sesionId);
            Persona aprendiz = buscarPersona(aprendizId);
            aprendiz.validarPuedeParticipar(reloj.ahora());
            String clave = Transaccion.claveReserva(sesion.id());
            Billetera walletCandidata = billeteras.buscarPorPersona(aprendizId)
                    .orElseThrow(() -> noEncontrado("No se encontró la billetera del aprendiz"));
            Billetera wallet = bloquearBilleteras(List.of(walletCandidata))
                    .get(walletCandidata.getId());
            Optional<Transaccion> existente = transacciones.buscarPorClaveIdempotencia(clave);
            if (existente.isPresent()) {
                if (sesion.estado() == Sesion.EstadoSesion.RESERVADA
                        && mismaPersona(sesion.aprendiz(), aprendiz)
                        && esMismaTransaccionSesion(
                        existente.get(),
                        Transaccion.TipoTransaccion.RESERVA,
                        sesion,
                        wallet,
                        wallet
                )) {
                    return sesion;
                }
                throw conflicto("La clave de reserva ya fue utilizada");
            }

            Sesion reservada = sesion.reservar(aprendiz);
            Transaccion reserva = new Transaccion(
                    null,
                    Transaccion.TipoTransaccion.RESERVA,
                    sesion.oferta().duracionHoras(),
                    wallet,
                    wallet,
                    reservada,
                    clave,
                    reloj.ahora()
            );
            reserva.aplicar();
            billeteras.guardar(wallet);
            transacciones.guardar(reserva);
            return sesiones.guardar(reservada);
        });
    }

    @Override
    public Sesion cancelarInscripcion(Long actorId, Long sesionId) {
        return unidadDeTrabajo.ejecutar(() -> {
            Sesion sesion = buscarSesion(sesionId);
            Persona actor = buscarPersona(actorId);
            Sesion cancelada = sesion.cancelar(actor, reloj.ahora());
            if (sesion.estado() == Sesion.EstadoSesion.RESERVADA) {
                String clave = Transaccion.claveLiberacion(sesion.id());
                Billetera walletCandidata =
                        billeteras.buscarPorPersona(sesion.aprendiz().getId())
                        .orElseThrow(() -> noEncontrado(
                                "No se encontró la billetera del aprendiz"));
                Billetera wallet = bloquearBilleteras(List.of(walletCandidata))
                        .get(walletCandidata.getId());
                Optional<Transaccion> existente =
                        transacciones.buscarPorClaveIdempotencia(clave);
                if (existente.isPresent()) {
                    if (!esMismaTransaccionSesion(
                            existente.get(),
                            Transaccion.TipoTransaccion.LIBERACION,
                            sesion,
                            wallet,
                            wallet
                    )) {
                        throw conflicto("La clave de liberación ya fue utilizada");
                    }
                } else {
                    Transaccion liberacion = new Transaccion(
                            null,
                            Transaccion.TipoTransaccion.LIBERACION,
                            sesion.oferta().duracionHoras(),
                            wallet,
                            wallet,
                            cancelada,
                            clave,
                            reloj.ahora()
                    );
                    liberacion.aplicar();
                    billeteras.guardar(wallet);
                    transacciones.guardar(liberacion);
                }
            }
            return sesiones.guardar(cancelada);
        });
    }

    @Override
    public Handshake iniciarHandshake(Long tutorId, Long sesionId) {
        return unidadDeTrabajo.ejecutar(() -> {
            Sesion sesion = buscarSesion(sesionId);
            Persona tutor = buscarPersona(tutorId);
            if (!mismaPersona(tutor, sesion.oferta().tutor())) {
                throw noAutorizado("Solo el tutor puede iniciar el handshake");
            }
            if (sesion.estado() != Sesion.EstadoSesion.RESERVADA) {
                throw conflicto("El handshake requiere una sesión RESERVADA");
            }
            if (evidencias.buscarPorSesion(sesionId).isPresent()) {
                throw conflicto("La sesión ya tiene una evidencia de handshake");
            }

            String token = generadorToken.generarTokenSesion();
            String codigo = generadorToken.generarCodigoCorto();
            if (token == null || !token.matches("[0-9a-fA-F]{32}")
                    || codigo == null || !codigo.matches("[0-9]{6}")) {
                throw new IllegalStateException(
                        "El generador debe producir un token de 128 bits y un código de seis dígitos");
            }
            OffsetDateTime ahora = reloj.ahora();
            Evidencia evidencia = new Evidencia(
                    null,
                    sesion,
                    sha256(token),
                    sha256(codigo),
                    ahora.plus(vigenciaHandshake),
                    null,
                    (short) 0,
                    null,
                    null,
                    null,
                    ahora
            );
            Evidencia guardada = evidencias.guardar(evidencia);
            return new Handshake(guardada, token, codigo);
        });
    }

    @Override
    public Sesion confirmarHandshake(ConfirmarHandshake solicitud) {
        validarHandshake(solicitud);
        Optional<Sesion> resultado = unidadDeTrabajo.ejecutar(() -> {
            Sesion sesion = buscarSesion(solicitud.sesionId());
            Persona aprendiz = buscarPersona(solicitud.aprendizId());
            if (!mismaPersona(aprendiz, sesion.aprendiz())) {
                throw noAutorizado("Solo el aprendiz asignado puede confirmar el handshake");
            }
            if (sesion.estado() != Sesion.EstadoSesion.RESERVADA) {
                throw conflicto("La sesión no está esperando el handshake");
            }

            String presentado = solicitud.token() != null
                    ? solicitud.token()
                    : solicitud.codigoCorto();
            Optional<Evidencia> consumida = evidencias.consumirSiUsable(
                    solicitud.sesionId(),
                    sha256(presentado),
                    reloj.ahora()
            );
            if (consumida.isEmpty()) {
                if (solicitud.codigoCorto() != null) {
                    evidencias.registrarIntentoFallidoSiUsable(
                            solicitud.sesionId(), reloj.ahora());
                }
                return Optional.empty();
            }

            Evidencia evidencia = consumida.get();
            Evidencia validada = new Evidencia(
                    evidencia.id(),
                    evidencia.sesion(),
                    evidencia.tokenHash(),
                    evidencia.codigoCortoHash(),
                    evidencia.expiraEn(),
                    evidencia.usadoEn(),
                    evidencia.intentosFallidos(),
                    null,
                    solicitud.latitud(),
                    solicitud.longitud(),
                    evidencia.marcaServidor()
            );
            String fotoUrl = solicitud.foto() == null ? null
                    : almacenEvidencias.guardar(
                    solicitud.sesionId(),
                    solicitud.foto(),
                    solicitud.tipoContenidoFoto()
            );
            Evidencia actualizada = new Evidencia(
                    validada.id(),
                    validada.sesion(),
                    validada.tokenHash(),
                    validada.codigoCortoHash(),
                    validada.expiraEn(),
                    validada.usadoEn(),
                    validada.intentosFallidos(),
                    fotoUrl,
                    validada.latitud(),
                    validada.longitud(),
                    validada.marcaServidor()
            );
            Sesion enCurso = sesion.confirmarHandshake(aprendiz);
            evidencias.guardar(actualizada);
            return Optional.of(sesiones.guardar(enCurso));
        });
        return resultado.orElseThrow(() -> conflicto(
                "El token o código es inválido, expiró, fue usado o está bloqueado"));
    }

    @Override
    public Sesion confirmarFinSesion(Long actorId, Long sesionId) {
        ConfirmacionFin resultado = unidadDeTrabajo.ejecutar(() -> {
            Sesion original = buscarSesion(sesionId);
            Persona actor = buscarPersona(actorId);
            Sesion actualizada = original.confirmarFin(actor, reloj.ahora());
            ExcedentePendiente excedente = null;
            if (actualizada.estado() == Sesion.EstadoSesion.COMPLETADA
                    && original.estado() != Sesion.EstadoSesion.COMPLETADA) {
                excedente = liquidar(actualizada);
                pasaporte.actualizarCredencialPorSesion(actualizada);
            }
            return new ConfirmacionFin(sesiones.guardar(actualizada), excedente);
        });
        if (resultado.excedente() != null) {
            notificadorTope.notificarExcedente(
                    resultado.excedente().personaId(),
                    resultado.excedente().horas()
            );
        }
        return resultado.sesion();
    }

    @Override
    public Reporte reportarProblema(ReportarProblema solicitud) {
        if (solicitud == null || solicitud.motivo() == null
                || solicitud.motivo().isBlank()) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El motivo del reporte es obligatorio");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Sesion sesion = buscarSesion(solicitud.sesionId());
            Persona autor = buscarPersona(solicitud.autorId());
            Sesion disputada = sesion.disputar(autor);
            Reporte reporte = new Reporte(
                    null,
                    disputada,
                    autor,
                    null,
                    solicitud.motivo(),
                    Reporte.EstadoReporte.ABIERTO,
                    reloj.ahora()
            );
            sesiones.guardar(disputada);
            return reportes.guardar(reporte);
        });
    }

    private ExcedentePendiente liquidar(Sesion sesion) {
        Oferta oferta = sesion.oferta();
        Billetera aprendizCandidata =
                billeteras.buscarPorPersona(sesion.aprendiz().getId())
                .orElseThrow(() -> noEncontrado("No se encontró la billetera del aprendiz"));
        Billetera destinoCandidato;
        Transaccion.TipoTransaccion tipo;
        switch (oferta.destinoHoras()) {
            case BILLETERA_PROPIA -> {
                tipo = Transaccion.TipoTransaccion.GANADA;
                destinoCandidato = billeteras.buscarPorPersona(oferta.tutor().getId())
                        .orElseThrow(() -> noEncontrado(
                                "No se encontró la billetera del tutor"));
            }
            case FONDO -> {
                tipo = Transaccion.TipoTransaccion.DONADA;
                destinoCandidato = billeteras.buscarSistema(Billetera.TipoBilletera.FONDO)
                        .orElseThrow(() -> noEncontrado(
                                "No está configurada la billetera FONDO"));
            }
            case REGALO -> {
                tipo = Transaccion.TipoTransaccion.DONADA;
                destinoCandidato = billeteras.buscarPorPersona(oferta.receptorRegalo().getId())
                        .orElseThrow(() -> noEncontrado(
                                "No se encontró la billetera del receptor del regalo"));
            }
            default -> throw new IllegalStateException("Destino de horas no soportado");
        }

        Billetera fondoCandidato = tipo == Transaccion.TipoTransaccion.GANADA
                ? billeteras.buscarSistema(Billetera.TipoBilletera.FONDO)
                .orElseThrow(() -> noEncontrado("No está configurada la billetera FONDO"))
                : null;
        List<Billetera> candidatas = new ArrayList<>(
                List.of(aprendizCandidata, destinoCandidato));
        if (fondoCandidato != null) {
            candidatas.add(fondoCandidato);
        }
        Map<Long, Billetera> bloqueadas = bloquearBilleteras(candidatas);
        Billetera aprendiz = bloqueadas.get(aprendizCandidata.getId());
        Billetera destino = bloqueadas.get(destinoCandidato.getId());
        Billetera fondo = fondoCandidato == null
                ? null
                : bloqueadas.get(fondoCandidato.getId());

        String clave = Transaccion.claveLiquidacion(sesion.id());
        Optional<Transaccion> existente = transacciones.buscarPorClaveIdempotencia(clave);
        boolean liquidacionNueva = existente.isEmpty();
        if (liquidacionNueva) {
            Transaccion liquidacion = new Transaccion(
                    null,
                    tipo,
                    oferta.duracionHoras(),
                    aprendiz,
                    destino,
                    sesion,
                    clave,
                    reloj.ahora()
            );
            liquidacion.aplicar();
            billeteras.guardar(aprendiz);
            billeteras.guardar(destino);
            transacciones.guardar(liquidacion);
        } else if (!esMismaTransaccionSesion(
                existente.get(), tipo, sesion, aprendiz, destino)) {
            throw conflicto("La clave de liquidación ya fue utilizada");
        }

        if (tipo == Transaccion.TipoTransaccion.GANADA && liquidacionNueva) {
            return aplicarTope(sesion, destino, fondo);
        }
        return null;
    }

    private ExcedentePendiente aplicarTope(
            Sesion sesion,
            Billetera tutor,
            Billetera fondo
    ) {
        BigDecimal limite = politicaTope.maximoSaldoDisponible();
        if (limite == null || limite.signum() <= 0) {
            throw new IllegalStateException("El tope de saldo no está configurado");
        }
        BigDecimal excedente = tutor.getSaldoDisponible().subtract(limite);
        if (excedente.signum() <= 0) {
            return null;
        }
        String clave = Transaccion.claveExcedente(sesion.id(), tutor.getId());
        if (transacciones.buscarPorClaveIdempotencia(clave).isEmpty()) {
            Transaccion transaccion = new Transaccion(
                    null,
                    Transaccion.TipoTransaccion.EXCEDENTE,
                    excedente,
                    tutor,
                    fondo,
                    sesion,
                    clave,
                    reloj.ahora()
            );
            transaccion.aplicar();
            billeteras.guardar(tutor);
            billeteras.guardar(fondo);
            transacciones.guardar(transaccion);
            return new ExcedentePendiente(tutor.getPersona().getId(), excedente);
        }
        return null;
    }

    private Sesion buscarSesion(Long id) {
        return sesiones.bloquearPorId(id)
                .orElseThrow(() -> noEncontrado("No se encontró la sesión"));
    }

    private Map<Long, Billetera> bloquearBilleteras(List<Billetera> candidatas) {
        List<Long> ids = candidatas.stream()
                .map(Billetera::getId)
                .distinct()
                .sorted()
                .toList();
        List<Billetera> bloqueadas = billeteras.bloquearPorIds(ids);
        if (bloqueadas.size() != ids.size()) {
            throw new IllegalStateException(
                    "No se pudieron bloquear todas las billeteras requeridas");
        }
        Map<Long, Billetera> porId = new HashMap<>();
        for (Billetera billetera : bloqueadas) {
            porId.put(billetera.getId(), billetera);
        }
        return porId;
    }

    private Persona buscarPersona(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Persona.class::isInstance)
                .map(Persona.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la persona"));
    }

    private static void validarHandshake(ConfirmarHandshake solicitud) {
        if (solicitud == null || solicitud.aprendizId() == null
                || solicitud.sesionId() == null
                || (solicitud.token() == null) == (solicitud.codigoCorto() == null)) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Indica sesión, aprendiz y exactamente un token o código");
        }
        if (solicitud.token() != null && solicitud.token().isBlank()
                || solicitud.codigoCorto() != null
                && !solicitud.codigoCorto().matches("[0-9]{6}")) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El token o código tiene un formato inválido");
        }
        boolean aportaEvidencia = solicitud.foto() != null || solicitud.latitud() != null
                || solicitud.longitud() != null;
        if (aportaEvidencia && !solicitud.consienteEvidencia()) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La foto o ubicación requiere consentimiento");
        }
        if (solicitud.foto() != null && (solicitud.tipoContenidoFoto() == null
                || solicitud.tipoContenidoFoto().isBlank())) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La foto requiere un tipo de contenido");
        }
        if (solicitud.foto() != null && solicitud.foto().length == 0) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La foto no puede estar vacía");
        }
        if (solicitud.foto() == null && solicitud.tipoContenidoFoto() != null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El tipo de contenido solo se informa junto con una foto");
        }
    }

    private static boolean esMismaTransaccionSesion(
            Transaccion transaccion,
            Transaccion.TipoTransaccion tipo,
            Sesion sesion,
            Billetera origen,
            Billetera destino
    ) {
        return transaccion.tipo() == tipo
                && transaccion.sesion() != null
                && Objects.equals(transaccion.sesion().id(), sesion.id())
                && transaccion.horas().compareTo(sesion.oferta().duracionHoras()) == 0
                && mismaBilletera(transaccion.billeteraOrigen(), origen)
                && mismaBilletera(transaccion.billeteraDestino(), destino);
    }

    private static boolean mismaBilletera(Billetera primera, Billetera segunda) {
        return primera == segunda
                || primera != null && segunda != null
                && primera.getId() != null && primera.getId().equals(segunda.getId());
    }

    private static String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(valor.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }

    private static boolean mismaPersona(Persona primera, Persona segunda) {
        return primera == segunda
                || primera != null && segunda != null
                && primera.getId() != null && primera.getId().equals(segunda.getId());
    }

    private static ErrorAplicacionException noEncontrado(String mensaje) {
        return error(ErrorAplicacionException.Codigo.NO_ENCONTRADO, mensaje);
    }

    private static ErrorAplicacionException noAutorizado(String mensaje) {
        return error(ErrorAplicacionException.Codigo.NO_AUTORIZADO, mensaje);
    }

    private static ErrorAplicacionException conflicto(String mensaje) {
        return error(ErrorAplicacionException.Codigo.CONFLICTO, mensaje);
    }

    private static ErrorAplicacionException error(
            ErrorAplicacionException.Codigo codigo,
            String mensaje
    ) {
        return new ErrorAplicacionException(codigo, mensaje);
    }

    private record ConfirmacionFin(Sesion sesion, ExcedentePendiente excedente) {
    }

    private record ExcedentePendiente(Long personaId, BigDecimal horas) {
    }
}
