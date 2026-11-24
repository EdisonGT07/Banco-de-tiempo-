package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Persona;

public interface DesbloqueadorPersona {
    Persona desbloquear(Long personaId);
}
