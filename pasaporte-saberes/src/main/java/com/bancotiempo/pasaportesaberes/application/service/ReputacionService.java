package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.ReputacionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioResena;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ReputacionService implements ReputacionUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioSesion sesiones;
    private final RepositorioResena resenas;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;
    private final PasaporteUseCases pasaporte;

    public ReputacionService(
            RepositorioUsuario usuarios,
            RepositorioSesion sesiones,
            RepositorioResena resenas,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            PasaporteUseCases pasaporte
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.sesiones = Objects.requireNonNull(sesiones);
        this.resenas = Objects.requireNonNull(resenas);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
        this.pasaporte = Objects.requireNonNull(pasaporte);
    }

    @Override
    public Resena calificarSesion(CalificarSesion solicitud) {
        if (solicitud == null || solicitud.autorId() == null || solicitud.sesionId() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El autor y la sesión son obligatorios");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Sesion sesion = sesiones.buscarPorId(solicitud.sesionId())
                    .orElseThrow(() -> noEncontrado("No se encontró la sesión"));
            Persona autor = buscarPersona(solicitud.autorId());
            List<Resena> existentes = resenas.listarPorSesion(sesion.id());
            Resena resena = Resena.crear(
                    null,
                    sesion,
                    autor,
                    solicitud.calificacion(),
                    solicitud.comentario(),
                    reloj.ahora()
            );
            resena.validarUnica(existentes);
            Resena guardada = resenas.guardar(resena);
            if (resena.autorRol() == Resena.AutorRol.APRENDIZ) {
                pasaporte.actualizarRatingPorResena(sesion);
            }
            return guardada;
        });
    }

    @Override
    public Reputacion obtenerReputacion(Long tutorId, Long habilidadId) {
        if (tutorId == null || habilidadId == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El tutor y la habilidad son obligatorios");
        }
        Persona tutor = buscarPersona(tutorId);
        List<Resena> recibidas = resenas.listarRecibidasPorTutor(tutorId);
        if (recibidas == null || recibidas.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("El repositorio devolvió reseñas inválidas");
        }
        List<Resena> ordenadas = recibidas.stream()
                .filter(resena -> resena.autorRol() == Resena.AutorRol.APRENDIZ
                        && mismaPersona(resena.sesion().oferta().tutor(), tutor))
                .sorted(Comparator.comparing(Resena::creadaEn)
                        .thenComparing(Resena::id,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
        BigDecimal sumaPonderada = BigDecimal.ZERO;
        BigDecimal sumaPesos = BigDecimal.ZERO;
        List<Persona> aprendices = new ArrayList<>();
        List<Integer> cantidades = new ArrayList<>();
        List<Persona> aprendicesDeHabilidad = new ArrayList<>();
        long cantidadResenas = 0;
        for (Resena resena : ordenadas) {
            Persona aprendiz = resena.sesion().aprendiz();
            int indice = indiceDe(aprendices, aprendiz);
            int previas = indice < 0 ? 0 : cantidades.get(indice);
            BigDecimal peso = switch (previas) {
                case 0 -> BigDecimal.ONE;
                case 1 -> new BigDecimal("0.50");
                default -> new BigDecimal("0.25");
            };
            boolean mismaHabilidad = Objects.equals(
                    resena.sesion().oferta().habilidad().id(), habilidadId);
            if (mismaHabilidad) {
                sumaPonderada = sumaPonderada.add(
                        BigDecimal.valueOf(resena.calificacion()).multiply(peso));
                sumaPesos = sumaPesos.add(peso);
                cantidadResenas++;
                if (aprendicesDeHabilidad.stream()
                        .noneMatch(persona -> mismaPersona(persona, aprendiz))) {
                    aprendicesDeHabilidad.add(aprendiz);
                }
            }
            if (indice < 0) {
                aprendices.add(aprendiz);
                cantidades.add(1);
            } else {
                cantidades.set(indice, previas + 1);
            }
        }
        BigDecimal promedio = sumaPesos.signum() == 0 ? null
                : sumaPonderada.divide(sumaPesos, 2, RoundingMode.HALF_UP);
        return new Reputacion(promedio, aprendicesDeHabilidad.size(), cantidadResenas);
    }

    private Persona buscarPersona(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Persona.class::isInstance)
                .map(Persona.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la persona"));
    }

    private static int indiceDe(List<Persona> personas, Persona buscada) {
        for (int indice = 0; indice < personas.size(); indice++) {
            if (mismaPersona(personas.get(indice), buscada)) {
                return indice;
            }
        }
        return -1;
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
