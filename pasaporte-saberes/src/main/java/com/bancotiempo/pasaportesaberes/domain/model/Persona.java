package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.PersonaInvalidaException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;

public class Persona extends Usuario {
    private static final int EDAD_MINIMA = 18;

    public enum Zona {
        URBANA,
        RURAL
    }

    public enum Estado {
        ACTIVO,
        SUSPENDIDO
    }

    private String docHmac;
    private LocalDate fechaNacimiento;
    private Zona zona;
    private Short estrato;
    private Estado estado = Estado.ACTIVO;
    private OffsetDateTime bloqueadoHasta;
    private Rango rangoEnsenanza;
    private Rango rangoAprendizaje;

    public String getDocHmac() {
        return docHmac;
    }

    public void setDocHmac(String docHmac) {
        this.docHmac = docHmac;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void registrarFechaNacimiento(LocalDate fechaNacimiento, LocalDate fechaActual) {
        validarEdad(fechaNacimiento, fechaActual);
        this.fechaNacimiento = fechaNacimiento;
    }

    public void validarMayorDeEdad(LocalDate fechaActual) {
        if (fechaNacimiento == null) {
            throw new PersonaInvalidaException("La fecha de nacimiento es obligatoria");
        }
        validarEdad(fechaNacimiento, fechaActual);
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Short getEstrato() {
        return estrato;
    }

    public void setEstrato(Short estrato) {
        if (estrato != null && (estrato < 1 || estrato > 6)) {
            throw new PersonaInvalidaException("El estrato debe estar entre 1 y 6");
        }
        this.estrato = estrato;
    }

    public Estado getEstado() {
        return estado;
    }

    public void suspender() {
        estado = Estado.SUSPENDIDO;
    }

    public void activar() {
        estado = Estado.ACTIVO;
    }

    public OffsetDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public boolean estaBloqueadaEn(OffsetDateTime ahora) {
        if (ahora == null) {
            throw new PersonaInvalidaException("La fecha de evaluación es obligatoria");
        }
        return estado == Estado.SUSPENDIDO
                || bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    public void validarPuedeParticipar(OffsetDateTime ahora) {
        if (estaBloqueadaEn(ahora)) {
            throw new PersonaInvalidaException(
                    "La persona está suspendida o tiene un bloqueo temporal vigente");
        }
    }

    public boolean aplicarBloqueoPorInasistencias(
            List<OffsetDateTime> fechasInasistencias,
            OffsetDateTime ahora,
            int umbral,
            Duration ventana,
            Duration duracionBloqueo
    ) {
        if (fechasInasistencias == null) {
            throw new PersonaInvalidaException("El historial de inasistencias es obligatorio");
        }
        if (ahora == null) {
            throw new PersonaInvalidaException("La fecha de evaluación es obligatoria");
        }
        if (umbral <= 0) {
            throw new PersonaInvalidaException("El umbral de inasistencias debe ser positivo");
        }
        validarDuracion(ventana, "La ventana de inasistencias");
        validarDuracion(duracionBloqueo, "La duración del bloqueo");

        OffsetDateTime inicioVentana = ahora.minus(ventana);
        long inasistenciasEnVentana = 0;
        for (OffsetDateTime fecha : fechasInasistencias) {
            if (fecha == null || fecha.isAfter(ahora)) {
                throw new PersonaInvalidaException(
                        "El historial contiene una fecha de inasistencia inválida");
            }
            if (!fecha.isBefore(inicioVentana)) {
                inasistenciasEnVentana++;
            }
        }
        if (inasistenciasEnVentana < umbral) {
            return false;
        }

        OffsetDateTime nuevoBloqueoHasta = ahora.plus(duracionBloqueo);
        if (bloqueadoHasta == null || bloqueadoHasta.isBefore(nuevoBloqueoHasta)) {
            bloqueadoHasta = nuevoBloqueoHasta;
        }
        return true;
    }

    public Rango getRangoEnsenanza() {
        return rangoEnsenanza;
    }

    public void setRangoEnsenanza(Rango rangoEnsenanza) {
        if (rangoEnsenanza != null
                && rangoEnsenanza.escala() != Rango.EscalaRango.ENSENANZA) {
            throw new PersonaInvalidaException(
                    "El rango de enseñanza debe pertenecer a la escala ENSENANZA");
        }
        this.rangoEnsenanza = rangoEnsenanza;
    }

    public Rango getRangoAprendizaje() {
        return rangoAprendizaje;
    }

    public void setRangoAprendizaje(Rango rangoAprendizaje) {
        if (rangoAprendizaje != null
                && rangoAprendizaje.escala() != Rango.EscalaRango.APRENDIZAJE) {
            throw new PersonaInvalidaException(
                    "El rango de aprendizaje debe pertenecer a la escala APRENDIZAJE");
        }
        this.rangoAprendizaje = rangoAprendizaje;
    }

    private static void validarEdad(LocalDate fechaNacimiento, LocalDate fechaActual) {
        if (fechaNacimiento == null) {
            throw new PersonaInvalidaException("La fecha de nacimiento es obligatoria");
        }
        if (fechaActual == null) {
            throw new PersonaInvalidaException("La fecha de evaluación es obligatoria");
        }
        if (fechaNacimiento.isAfter(fechaActual)) {
            throw new PersonaInvalidaException("La fecha de nacimiento no puede ser futura");
        }
        if (Period.between(fechaNacimiento, fechaActual).getYears() < EDAD_MINIMA) {
            throw new PersonaInvalidaException("La persona debe tener al menos 18 años");
        }
    }

    private static void validarDuracion(Duration duracion, String nombre) {
        if (duracion == null || duracion.isNegative() || duracion.isZero()) {
            throw new PersonaInvalidaException(nombre + " debe ser mayor que cero");
        }
    }
}
