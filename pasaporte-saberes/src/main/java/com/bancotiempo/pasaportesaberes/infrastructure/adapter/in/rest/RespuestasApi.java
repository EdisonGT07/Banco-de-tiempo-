package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.SesionesUseCases;
import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;
import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class RespuestasApi {
    private RespuestasApi() {
    }

    static OfertaResponse oferta(Oferta oferta) {
        return new OfertaResponse(
                oferta.id(),
                oferta.tutor().getId(),
                oferta.tutor().getNombre(),
                oferta.habilidad().id(),
                oferta.habilidad().nombre(),
                oferta.descripcion(),
                oferta.duracionHoras(),
                oferta.modalidad(),
                oferta.barrio(),
                oferta.destinoHoras(),
                oferta.receptorRegalo() == null ? null : oferta.receptorRegalo().getId(),
                oferta.activa(),
                oferta.creadaEn()
        );
    }

    static SesionResponse sesion(Sesion sesion) {
        return new SesionResponse(
                sesion.id(),
                sesion.oferta().id(),
                sesion.oferta().tutor().getId(),
                sesion.aprendiz() == null ? null : sesion.aprendiz().getId(),
                sesion.iniciaEn(),
                sesion.lugarOEnlace(),
                sesion.estado(),
                sesion.finConfirmacionTutor(),
                sesion.finConfirmacionAprendiz(),
                sesion.inasistente(),
                sesion.version(),
                sesion.creadaEn()
        );
    }

    static SolicitudAprendizajeResponse solicitudAprendizaje(QuererAprender solicitud) {
        return new SolicitudAprendizajeResponse(
                solicitud.id().personaId(),
                solicitud.id().habilidadId(),
                solicitud.habilidad().nombre(),
                solicitud.creadaEn()
        );
    }

    static BilleteraResponse billetera(Billetera billetera) {
        return new BilleteraResponse(
                billetera.getId(),
                billetera.getTipo(),
                billetera.getSaldoDisponible(),
                billetera.getSaldoReservado(),
                billetera.getVersion()
        );
    }

    static TransaccionResponse transaccion(Transaccion transaccion) {
        return new TransaccionResponse(
                transaccion.id(),
                transaccion.tipo(),
                transaccion.horas(),
                transaccion.billeteraOrigen().getId(),
                transaccion.billeteraDestino().getId(),
                transaccion.sesion() == null ? null : transaccion.sesion().id(),
                transaccion.creadaEn()
        );
    }

    static ReporteResponse reporte(Reporte reporte) {
        return new ReporteResponse(
                reporte.id(),
                reporte.sesion().id(),
                reporte.autor().getId(),
                reporte.revisor() == null ? null : reporte.revisor().getId(),
                reporte.motivo(),
                reporte.estado(),
                reporte.creadoEn()
        );
    }

    static ResenaResponse resena(Resena resena) {
        return new ResenaResponse(
                resena.id(),
                resena.sesion().id(),
                resena.autor().getId(),
                resena.autorRol(),
                resena.calificacion(),
                resena.comentario(),
                resena.creadaEn()
        );
    }

    static AvalResponse aval(Aval aval) {
        if (aval == null) {
            return null;
        }
        return new AvalResponse(
                aval.id(),
                aval.entidad().getId(),
                aval.credencial().id(),
                aval.fecha(),
                aval.vigenciaHasta(),
                aval.estado()
        );
    }

    static PasaporteResponse pasaporte(PasaporteUseCases.Pasaporte pasaporte) {
        List<CredencialResponse> credenciales = pasaporte.credenciales().stream()
                .map(RespuestasApi::credencial)
                .toList();
        return new PasaporteResponse(pasaporte.personaId(), pasaporte.nombre(), credenciales);
    }

    static VerificacionResponse verificacion(PasaporteUseCases.Verificacion verificacion) {
        return new VerificacionResponse(
                credencial(verificacion.credencial()),
                verificacion.firmaValida(),
                verificacion.numeroVerificaciones()
        );
    }

    static RangosResponse rangos(PasaporteUseCases.Rangos rangos) {
        return new RangosResponse(rango(rangos.ensenanza()), rango(rangos.aprendizaje()));
    }

    static PersonaAdministradaResponse persona(Persona persona) {
        return new PersonaAdministradaResponse(
                persona.getId(),
                persona.getNombre(),
                persona.getCorreo(),
                persona.getEstado()
        );
    }

    static HandshakeResponse handshake(SesionesUseCases.Handshake handshake) {
        return new HandshakeResponse(
                handshake.evidencia().id(),
                handshake.evidencia().expiraEn(),
                handshake.token(),
                handshake.codigoCorto()
        );
    }

    private static CredencialResponse credencial(PasaporteUseCases.CredencialPublica credencial) {
        return new CredencialResponse(
                credencial.id(),
                credencial.habilidad(),
                credencial.horasEnsenadas(),
                credencial.rating(),
                credencial.nivel(),
                aval(credencial.aval())
        );
    }

    private static RangoResponse rango(Rango rango) {
        if (rango == null) {
            return null;
        }
        return new RangoResponse(
                rango.id(),
                rango.escala(),
                rango.nombre(),
                rango.orden(),
                rango.minimoHoras(),
                rango.minimoRating(),
                rango.minimoAprendices(),
                rango.minimoHabilidades(),
                rango.minimoAsistencia()
        );
    }

    public record OfertaResponse(
            Long id,
            Long tutorId,
            String nombreTutor,
            Long habilidadId,
            String habilidad,
            String descripcion,
            BigDecimal duracionHoras,
            Oferta.Modalidad modalidad,
            String barrio,
            Oferta.DestinoHoras destinoHoras,
            Long receptorRegaloId,
            boolean activa,
            OffsetDateTime creadaEn
    ) {
    }

    public record SesionResponse(
            Long id,
            Long ofertaId,
            Long tutorId,
            Long aprendizId,
            OffsetDateTime iniciaEn,
            String lugarOEnlace,
            Sesion.EstadoSesion estado,
            OffsetDateTime finConfirmacionTutor,
            OffsetDateTime finConfirmacionAprendiz,
            Sesion.Inasistente inasistente,
            long version,
            OffsetDateTime creadaEn
    ) {
    }

    public record SolicitudAprendizajeResponse(
            Long personaId,
            Long habilidadId,
            String habilidad,
            OffsetDateTime creadaEn
    ) {
    }

    public record BilleteraResponse(
            Long id,
            Billetera.TipoBilletera tipo,
            BigDecimal saldoDisponible,
            BigDecimal saldoReservado,
            long version
    ) {
    }

    public record TransaccionResponse(
            Long id,
            Transaccion.TipoTransaccion tipo,
            BigDecimal horas,
            Long billeteraOrigenId,
            Long billeteraDestinoId,
            Long sesionId,
            OffsetDateTime creadaEn
    ) {
    }

    public record ReporteResponse(
            Long id,
            Long sesionId,
            Long autorId,
            Long revisorId,
            String motivo,
            Reporte.EstadoReporte estado,
            OffsetDateTime creadoEn
    ) {
    }

    public record ResenaResponse(
            Long id,
            Long sesionId,
            Long autorId,
            Resena.AutorRol autorRol,
            short calificacion,
            String comentario,
            OffsetDateTime creadaEn
    ) {
    }

    public record AvalResponse(
            Long id,
            Long entidadId,
            Long credencialId,
            LocalDate fecha,
            LocalDate vigenciaHasta,
            Aval.EstadoAval estado
    ) {
    }

    public record PasaporteResponse(
            Long personaId,
            String nombre,
            List<CredencialResponse> credenciales
    ) {
    }

    public record CredencialResponse(
            Long id,
            String habilidad,
            BigDecimal horasEnsenadas,
            BigDecimal rating,
            Credencial.Nivel nivel,
            AvalResponse aval
    ) {
    }

    public record VerificacionResponse(
            CredencialResponse credencial,
            boolean firmaValida,
            long numeroVerificaciones
    ) {
    }

    public record RangosResponse(RangoResponse ensenanza, RangoResponse aprendizaje) {
    }

    public record RangoResponse(
            Long id,
            Rango.EscalaRango escala,
            String nombre,
            short orden,
            BigDecimal minimoHoras,
            BigDecimal minimoRating,
            Integer minimoAprendices,
            Integer minimoHabilidades,
            BigDecimal minimoAsistencia
    ) {
    }

    public record PersonaAdministradaResponse(
            Long id,
            String nombre,
            String correo,
            Persona.Estado estado
    ) {
    }

    public record HandshakeResponse(
            Long evidenciaId,
            OffsetDateTime expiraEn,
            String token,
            String codigoCorto
    ) {
    }
}
