package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.HorasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.PoliticaFondo;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioTransaccion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public final class HorasService implements HorasUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioBilletera billeteras;
    private final RepositorioTransaccion transacciones;
    private final PoliticaFondo politicaFondo;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;

    public HorasService(
            RepositorioUsuario usuarios,
            RepositorioBilletera billeteras,
            RepositorioTransaccion transacciones,
            PoliticaFondo politicaFondo,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.billeteras = Objects.requireNonNull(billeteras);
        this.transacciones = Objects.requireNonNull(transacciones);
        this.politicaFondo = Objects.requireNonNull(politicaFondo);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public Billetera consultarBilletera(Long personaId) {
        buscarPersona(personaId);
        return billeteras.buscarPorPersona(personaId)
                .orElseThrow(() -> noEncontrado("No se encontró la billetera"));
    }

    @Override
    public Transaccion retirarDelFondo(RetiroFondo solicitud) {
        validarSolicitud(solicitud);
        return unidadDeTrabajo.ejecutar(() -> {
            var transaccionExistente =
                    transacciones.buscarPorClaveIdempotencia(solicitud.claveIdempotencia());
            if (transaccionExistente.isPresent()) {
                Transaccion existente = transaccionExistente.get();
                if (esMismoRetiro(existente, solicitud)) {
                    return existente;
                }
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "La clave de idempotencia ya se usó para otro movimiento");
            }

            Persona persona = buscarPersona(solicitud.personaId());
            Billetera wallet = billeteras.buscarPorPersona(persona.getId())
                    .orElseThrow(() -> noEncontrado("No se encontró la billetera personal"));
            if (!politicaFondo.puedeRetirar(persona, wallet)) {
                throw error(ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                        "La persona no cumple los requisitos para retirar del fondo");
            }
            YearMonth periodo = YearMonth.from(reloj.hoy());
            BigDecimal retirado = transacciones.sumarRetiros(persona.getId(), periodo);
            BigDecimal limite = politicaFondo.maximoRetiroPorMes();
            if (limite == null || limite.signum() <= 0
                    || retirado.add(solicitud.horas()).compareTo(limite) > 0) {
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "El retiro supera el máximo mensual configurado");
            }

            Billetera fondo = billeteras.buscarSistema(Billetera.TipoBilletera.FONDO)
                    .orElseThrow(() -> noEncontrado(
                            "No está configurada la billetera FONDO"));
            Transaccion transaccion = new Transaccion(
                    null,
                    Transaccion.TipoTransaccion.RETIRO_FONDO,
                    solicitud.horas(),
                    fondo,
                    wallet,
                    null,
                    solicitud.claveIdempotencia(),
                    reloj.ahora()
            );
            transaccion.aplicar();
            billeteras.guardar(fondo);
            billeteras.guardar(wallet);
            return transacciones.guardar(transaccion);
        });
    }

    private Persona buscarPersona(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Persona.class::isInstance)
                .map(Persona.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la persona"));
    }

    private static void validarSolicitud(RetiroFondo solicitud) {
        if (solicitud == null || solicitud.personaId() == null
                || solicitud.horas() == null || solicitud.horas().signum() <= 0
                || solicitud.claveIdempotencia() == null
                || solicitud.claveIdempotencia().isBlank()) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La persona, las horas y la clave de idempotencia son obligatorias");
        }
    }

    private static boolean esMismoRetiro(Transaccion existente, RetiroFondo solicitud) {
        return existente.tipo() == Transaccion.TipoTransaccion.RETIRO_FONDO
                && existente.horas().compareTo(solicitud.horas()) == 0
                && existente.billeteraDestino() != null
                && existente.billeteraDestino().getPersona() != null
                && Objects.equals(
                existente.billeteraDestino().getPersona().getId(),
                solicitud.personaId()
        );
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
