package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasPasaporte;
import com.bancotiempo.pasaportesaberes.application.port.out.FirmadorCredenciales;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorCertificadoPdf;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorCodigoVerificacion;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioAval;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioCredencial;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioRango;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioResena;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.application.port.out.VerificadorCredenciales;
import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PasaporteService implements PasaporteUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioCredencial credenciales;
    private final RepositorioAval avales;
    private final RepositorioRango rangos;
    private final RepositorioResena resenas;
    private final ConsultasPasaporte consultas;
    private final GeneradorCodigoVerificacion codigos;
    private final FirmadorCredenciales firmador;
    private final VerificadorCredenciales verificador;
    private final GeneradorCertificadoPdf pdf;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;
    private final BigDecimal ratingMinimoValidacion;
    private final int aprendicesMinimosValidacion;

    public PasaporteService(
            RepositorioUsuario usuarios,
            RepositorioCredencial credenciales,
            RepositorioAval avales,
            RepositorioRango rangos,
            RepositorioResena resenas,
            ConsultasPasaporte consultas,
            GeneradorCodigoVerificacion codigos,
            FirmadorCredenciales firmador,
            VerificadorCredenciales verificador,
            GeneradorCertificadoPdf pdf,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            BigDecimal ratingMinimoValidacion,
            int aprendicesMinimosValidacion
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.credenciales = Objects.requireNonNull(credenciales);
        this.avales = Objects.requireNonNull(avales);
        this.rangos = Objects.requireNonNull(rangos);
        this.resenas = Objects.requireNonNull(resenas);
        this.consultas = Objects.requireNonNull(consultas);
        this.codigos = Objects.requireNonNull(codigos);
        this.firmador = Objects.requireNonNull(firmador);
        this.verificador = Objects.requireNonNull(verificador);
        this.pdf = Objects.requireNonNull(pdf);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
        if (ratingMinimoValidacion == null || ratingMinimoValidacion.compareTo(BigDecimal.ONE) < 0
                || ratingMinimoValidacion.compareTo(BigDecimal.valueOf(5)) > 0
                || aprendicesMinimosValidacion <= 0) {
            throw new IllegalArgumentException("La política de validación es inválida");
        }
        this.ratingMinimoValidacion = ratingMinimoValidacion;
        this.aprendicesMinimosValidacion = aprendicesMinimosValidacion;
    }

    @Override
    public Pasaporte obtenerPasaporte(Long personaId) {
        Persona persona = buscarPersona(personaId);
        List<Credencial> dePersona = credenciales.listarPorPersona(personaId);
        if (dePersona == null) {
            throw new IllegalStateException("El repositorio devolvió credenciales nulas");
        }
        List<CredencialPublica> publicas = dePersona.stream()
                .map(this::convertirPublica)
                .toList();
        return new Pasaporte(personaId, persona.getNombre(), publicas);
    }

    @Override
    public Verificacion verificarCredencial(String codigoVerificacion) {
        if (codigoVerificacion == null || codigoVerificacion.isBlank()) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El código de verificación es obligatorio");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Credencial credencial = credenciales.buscarPorCodigo(codigoVerificacion)
                    .orElseThrow(() -> noEncontrado("No se encontró la credencial"));
            boolean firmaValida = verificador.verificar(credencial);
            if (firmaValida) {
                Credencial verificada = credencial.registrarVerificacion(reloj.ahora());
                String nuevaFirma = firmar(verificada);
                credenciales.guardar(new Credencial(
                        verificada.id(),
                        verificada.persona(),
                        verificada.habilidad(),
                        verificada.horasEnsenadas(),
                        verificada.ratingPromedio(),
                        verificada.codigoVerificacion(),
                        nuevaFirma,
                        verificada.actualizadaEn(),
                        verificada.numeroVerificaciones()
                ));
            }
            return new Verificacion(
                    convertirPublica(credencial),
                    firmaValida,
                    firmaValida ? credencial.numeroVerificaciones() + 1
                            : credencial.numeroVerificaciones()
            );
        });
    }

    @Override
    public byte[] emitirCertificadoPdf(Long credencialId) {
        Credencial credencial = credenciales.buscarPorId(credencialId)
                .orElseThrow(() -> noEncontrado("No se encontró la credencial"));
        if (!verificador.verificar(credencial)) {
            throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                    "La firma de la credencial no es válida");
        }
        return pdf.generar(convertirPublica(credencial), credencial.codigoVerificacion());
    }

    @Override
    public Rangos calcularRangos(Long personaId) {
        return unidadDeTrabajo.ejecutar(() -> calcularRangosEnUnidad(personaId));
    }

    @Override
    public void actualizarCredencialPorSesion(Sesion sesionCompletada) {
        if (sesionCompletada == null
                || sesionCompletada.estado() != Sesion.EstadoSesion.COMPLETADA) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Solo una sesión completada puede actualizar el pasaporte");
        }
        unidadDeTrabajo.ejecutar(() -> {
            Persona tutor = sesionCompletada.oferta().tutor();
            Long habilidadId = sesionCompletada.oferta().habilidad().id();
            Credencial existente = credenciales.buscarPorPersonaYHabilidad(
                    tutor.getId(), habilidadId).orElse(null);
            BigDecimal horasPrevias = existente == null
                    ? BigDecimal.ZERO.setScale(2)
                    : existente.horasEnsenadas();
            BigDecimal horas = horasPrevias.add(
                    sesionCompletada.oferta().duracionHoras());
            List<Resena> recibidas = resenas.listarRecibidasPorTutor(tutor.getId());
            OffsetDateTime ahora = reloj.ahora();
            String codigo = existente == null
                    ? validarCodigo(codigos.generar())
                    : existente.codigoVerificacion();
            Credencial calculadora = existente == null
                    ? new Credencial(
                    null,
                    tutor,
                    sesionCompletada.oferta().habilidad(),
                    horas,
                    null,
                    codigo,
                    firmador.firmar(tutor.getId(), habilidadId, horas, null, codigo, ahora),
                    ahora,
                    0
            )
                    : existente;
            BigDecimal rating = calculadora.calcularRatingPonderado(recibidas);
            String firma = firmador.firmar(
                    tutor.getId(),
                    habilidadId,
                    horas,
                    rating,
                    codigo,
                    ahora
            );
            Credencial actualizada = existente == null
                    ? new Credencial(null, tutor, sesionCompletada.oferta().habilidad(),
                    horas, rating, codigo, firma, ahora, 0)
                    : existente.actualizarEstadisticas(horas, rating, firma, ahora);
            credenciales.guardar(actualizada);
            calcularRangosEnUnidad(tutor.getId());
            return null;
        });
    }

    @Override
    public void actualizarRatingPorResena(Sesion sesionCompletada) {
        if (sesionCompletada == null
                || sesionCompletada.estado() != Sesion.EstadoSesion.COMPLETADA) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La reseña debe pertenecer a una sesión completada");
        }
        unidadDeTrabajo.ejecutar(() -> {
            Persona tutor = sesionCompletada.oferta().tutor();
            Long habilidadId = sesionCompletada.oferta().habilidad().id();
            Credencial credencial = credenciales.buscarPorPersonaYHabilidad(
                    tutor.getId(), habilidadId)
                    .orElseThrow(() -> error(ErrorAplicacionException.Codigo.CONFLICTO,
                            "La sesión no tiene una credencial asociada"));
            BigDecimal rating = credencial.calcularRatingPonderado(
                    resenas.listarRecibidasPorTutor(tutor.getId()));
            OffsetDateTime ahora = reloj.ahora();
            String firma = firmador.firmar(
                    tutor.getId(),
                    habilidadId,
                    credencial.horasEnsenadas(),
                    rating,
                    credencial.codigoVerificacion(),
                    ahora
            );
            credenciales.guardar(credencial.actualizarEstadisticas(
                    credencial.horasEnsenadas(), rating, firma, ahora));
            calcularRangosEnUnidad(tutor.getId());
            return null;
        });
    }

    private CredencialPublica convertirPublica(Credencial credencial) {
        List<Aval> listaAvales = avales.listarPorCredencial(credencial.id());
        Aval avalVigente = listaAvales.stream()
                .filter(av -> av.estaVigenteEn(reloj.hoy()))
                .max(Comparator.comparing(Aval::vigenciaHasta))
                .orElse(null);
        List<Resena> recibidas = resenas.listarRecibidasPorTutor(
                credencial.persona().getId());
        int aprendices = contarAprendicesDeHabilidad(credencial, recibidas);
        Credencial.Nivel nivel = credencial.calcularNivel(
                aprendices,
                ratingMinimoValidacion,
                aprendicesMinimosValidacion,
                avalVigente,
                reloj.hoy()
        );
        return new CredencialPublica(
                credencial.id(),
                credencial.habilidad().nombre(),
                credencial.horasEnsenadas(),
                credencial.ratingPromedio(),
                nivel,
                avalVigente
        );
    }

    private ConsultasPasaporte.Estadisticas obtenerEstadisticas(
            Long personaId,
            Rango.EscalaRango escala
    ) {
        ConsultasPasaporte.Estadisticas estadisticas =
                consultas.obtenerEstadisticas(personaId, escala);
        if (estadisticas == null) {
            throw new IllegalStateException("La consulta de pasaporte devolvió null");
        }
        return estadisticas;
    }

    private Rangos calcularRangosEnUnidad(Long personaId) {
        Persona persona = buscarPersona(personaId);
        ConsultasPasaporte.Estadisticas ensenanza = obtenerEstadisticas(
                personaId, Rango.EscalaRango.ENSENANZA);
        ConsultasPasaporte.Estadisticas aprendizaje = obtenerEstadisticas(
                personaId, Rango.EscalaRango.APRENDIZAJE);
        Rango rangoEnsenanza = seleccionarRango(Rango.EscalaRango.ENSENANZA, ensenanza);
        Rango rangoAprendizaje = seleccionarRango(Rango.EscalaRango.APRENDIZAJE, aprendizaje);
        persona.setRangoEnsenanza(rangoEnsenanza);
        persona.setRangoAprendizaje(rangoAprendizaje);
        usuarios.guardarPersona(persona);
        return new Rangos(rangoEnsenanza, rangoAprendizaje);
    }

    private Rango seleccionarRango(
            Rango.EscalaRango escala,
            ConsultasPasaporte.Estadisticas estadisticas
    ) {
        List<Rango> catalogo = rangos.listarPorEscala(escala);
        if (catalogo == null) {
            throw new IllegalStateException("El repositorio devolvió un catálogo nulo");
        }
        return Rango.seleccionarRango(
                catalogo,
                escala,
                estadisticas.horas(),
                estadisticas.rating(),
                estadisticas.aprendicesDistintos(),
                estadisticas.habilidades(),
                estadisticas.asistencia()
        ).orElse(null);
    }

    private int contarAprendicesDeHabilidad(Credencial credencial, List<Resena> recibidas) {
        if (recibidas == null) {
            throw new IllegalStateException("El repositorio devolvió reseñas nulas");
        }
        List<Persona> aprendices = new ArrayList<>();
        for (Resena resena : recibidas) {
            if (resena.autorRol() == Resena.AutorRol.APRENDIZ
                    && mismaPersona(resena.sesion().oferta().tutor(), credencial.persona())
                    && Objects.equals(
                    resena.sesion().oferta().habilidad().id(), credencial.habilidad().id())) {
                Persona aprendiz = resena.sesion().aprendiz();
                if (aprendices.stream().noneMatch(p -> mismaPersona(p, aprendiz))) {
                    aprendices.add(aprendiz);
                }
            }
        }
        return aprendices.size();
    }

    private String firmar(Credencial credencial) {
        return firmador.firmar(
                credencial.persona().getId(),
                credencial.habilidad().id(),
                credencial.horasEnsenadas(),
                credencial.ratingPromedio(),
                credencial.codigoVerificacion(),
                credencial.actualizadaEn()
        );
    }

    private static String validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank() || codigo.length() > 32) {
            throw new IllegalStateException(
                    "El generador produjo un código de verificación inválido");
        }
        return codigo;
    }

    private Persona buscarPersona(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Persona.class::isInstance)
                .map(Persona.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la persona"));
    }

    private static boolean mismaPersona(Persona primera, Persona segunda) {
        return primera == segunda
                || primera != null && segunda != null
                && primera.getId() != null && primera.getId().equals(segunda.getId());
    }

    private static ErrorAplicacionException noEncontrado(String mensaje) {
        return error(ErrorAplicacionException.Codigo.NO_ENCONTRADO, mensaje);
    }

    private static ErrorAplicacionException error(
            ErrorAplicacionException.Codigo codigo,
            String mensaje
    ) {
        return new ErrorAplicacionException(codigo, mensaje);
    }
}
