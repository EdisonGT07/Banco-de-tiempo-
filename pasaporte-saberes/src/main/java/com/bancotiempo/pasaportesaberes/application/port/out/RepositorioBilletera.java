package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Billetera;

import java.util.Optional;

public interface RepositorioBilletera {
    Optional<Billetera> buscarPorPersona(Long personaId);

    Optional<Billetera> buscarSistema(Billetera.TipoBilletera tipo);

    Billetera guardar(Billetera billetera);
}
