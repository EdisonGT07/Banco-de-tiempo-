package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.math.BigDecimal;
import java.util.List;

public interface PasaporteUseCases {
    Pasaporte obtenerPasaporte(Long personaId);

    Verificacion verificarCredencial(String codigoVerificacion);

    byte[] emitirCertificadoPdf(Long credencialId);

    Rangos calcularRangos(Long personaId);

    void actualizarCredencialPorSesion(Sesion sesionCompletada);

    void actualizarRatingPorResena(Sesion sesionCompletada);

    record Pasaporte(Long personaId, String nombre, List<CredencialPublica> credenciales) {
        public Pasaporte {
            credenciales = List.copyOf(credenciales);
        }
    }

    record CredencialPublica(
            Long id,
            String habilidad,
            BigDecimal horasEnsenadas,
            BigDecimal rating,
            Credencial.Nivel nivel,
            Aval aval
    ) {
    }

    record Verificacion(
            CredencialPublica credencial,
            boolean firmaValida,
            long numeroVerificaciones
    ) {
    }

    record Rangos(Rango ensenanza, Rango aprendizaje) {
    }
}
