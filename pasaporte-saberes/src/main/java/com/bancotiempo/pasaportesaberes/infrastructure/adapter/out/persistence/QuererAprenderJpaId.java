package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuererAprenderJpaId implements Serializable {
    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @Column(name = "habilidad_id", nullable = false)
    private Long habilidadId;

    protected QuererAprenderJpaId() {
    }

    public QuererAprenderJpaId(Long personaId, Long habilidadId) {
        this.personaId = personaId;
        this.habilidadId = habilidadId;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public Long getHabilidadId() {
        return habilidadId;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof QuererAprenderJpaId that)) {
            return false;
        }
        return Objects.equals(personaId, that.personaId)
                && Objects.equals(habilidadId, that.habilidadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personaId, habilidadId);
    }
}
