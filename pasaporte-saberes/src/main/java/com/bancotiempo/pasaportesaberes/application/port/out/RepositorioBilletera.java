package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Billetera;

import java.util.List;
import java.util.Optional;

public interface RepositorioBilletera {
    Optional<Billetera> buscarPorId(Long id);

    Optional<Billetera> buscarPorPersona(Long personaId);

    Optional<Billetera> buscarSistema(Billetera.TipoBilletera tipo);

    List<Billetera> bloquearPorIds(List<Long> ids);

    Billetera guardar(Billetera billetera);
}
