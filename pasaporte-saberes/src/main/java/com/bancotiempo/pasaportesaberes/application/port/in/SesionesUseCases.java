package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Evidencia;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.math.BigDecimal;

public interface SesionesUseCases {
    Sesion inscribirseEnSesion(Long aprendizId, Long sesionId);

    Sesion cancelarInscripcion(Long actorId, Long sesionId);

    Handshake iniciarHandshake(Long tutorId, Long sesionId);

    Sesion confirmarHandshake(ConfirmarHandshake solicitud);

    Sesion confirmarFinSesion(Long actorId, Long sesionId);

    Reporte reportarProblema(ReportarProblema solicitud);

    record Handshake(Evidencia evidencia, String token, String codigoCorto) {
    }

    record ConfirmarHandshake(
            Long aprendizId,
            Long sesionId,
            String token,
            String codigoCorto,
            byte[] foto,
            String tipoContenidoFoto,
            BigDecimal latitud,
            BigDecimal longitud,
            boolean consienteEvidencia
    ) {
        public ConfirmarHandshake {
            foto = foto == null ? null : foto.clone();
        }

        @Override
        public byte[] foto() {
            return foto == null ? null : foto.clone();
        }
    }

    record ReportarProblema(Long autorId, Long sesionId, String motivo) {
    }
}
