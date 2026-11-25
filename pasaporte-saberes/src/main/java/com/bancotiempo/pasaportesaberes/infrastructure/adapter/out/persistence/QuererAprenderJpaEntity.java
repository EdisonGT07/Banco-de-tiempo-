package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.OffsetDateTime;

@Entity
@Table(name = "querer_aprender")
public class QuererAprenderJpaEntity {
    @EmbeddedId
    private QuererAprenderJpaId id;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    protected QuererAprenderJpaEntity() {
    }

    public QuererAprenderJpaId getId() {
        return id;
    }

    public void setId(QuererAprenderJpaId id) {
        this.id = id;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(OffsetDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }
}
