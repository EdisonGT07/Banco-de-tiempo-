package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.notification;

import com.bancotiempo.pasaportesaberes.application.port.out.NotificadorTope;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NotificadorTopeSmtp implements NotificadorTope {
    private final CorreoSmtp correo;
    private final RepositorioUsuario usuarios;

    public NotificadorTopeSmtp(CorreoSmtp correo, RepositorioUsuario usuarios) {
        this.correo = correo;
        this.usuarios = usuarios;
    }

    @Override
    public void notificarExcedente(Long personaId, BigDecimal horasTransferidas) {
        if (personaId == null || horasTransferidas == null || horasTransferidas.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La persona y las horas transferidas deben ser válidas");
        }
        Usuario usuario = usuarios.buscarPorId(personaId).orElseThrow(
                () -> new IllegalStateException(
                        "No existe la persona que debe recibir el aviso de excedente"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException(
                    "El destinatario del aviso de excedente debe ser una persona");
        }
        correo.enviar(
                persona.getCorreo(),
                "Tu saldo alcanzó el tope",
                "Se transfirieron " + horasTransferidas.toPlainString()
                        + " horas excedentes al fondo comunitario."
        );
    }
}
