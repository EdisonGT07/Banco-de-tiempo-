package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sesion")
public class SesionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oferta_id", nullable = false)
    private Long ofertaId;

    @Column(name = "aprendiz_id")
    private Long aprendizId;

    @Column(name = "inicia_en", nullable = false)
    private OffsetDateTime iniciaEn;

    @Column(name = "lugar_o_enlace", length = 300)
    private String lugarOEnlace;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 24)
    private Sesion.EstadoSesion estado;

    @Column(name = "fin_conf_tutor_en")
    private OffsetDateTime finConfirmacionTutor;

    @Column(name = "fin_conf_apr_en")
    private OffsetDateTime finConfirmacionAprendiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "inasistente", length = 10)
    private Sesion.Inasistente inasistente;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    protected SesionJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getOfertaId() {
        return ofertaId;
    }

    public void setOfertaId(Long ofertaId) {
        this.ofertaId = ofertaId;
    }

    public Long getAprendizId() {
        return aprendizId;
    }

    public void setAprendizId(Long aprendizId) {
        this.aprendizId = aprendizId;
    }

    public OffsetDateTime getIniciaEn() {
        return iniciaEn;
    }

    public void setIniciaEn(OffsetDateTime iniciaEn) {
        this.iniciaEn = iniciaEn;
    }

    public String getLugarOEnlace() {
        return lugarOEnlace;
    }

    public void setLugarOEnlace(String lugarOEnlace) {
        this.lugarOEnlace = lugarOEnlace;
    }

    public Sesion.EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(Sesion.EstadoSesion estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFinConfirmacionTutor() {
        return finConfirmacionTutor;
    }

    public void setFinConfirmacionTutor(OffsetDateTime finConfirmacionTutor) {
        this.finConfirmacionTutor = finConfirmacionTutor;
    }

    public OffsetDateTime getFinConfirmacionAprendiz() {
        return finConfirmacionAprendiz;
    }

    public void setFinConfirmacionAprendiz(OffsetDateTime finConfirmacionAprendiz) {
        this.finConfirmacionAprendiz = finConfirmacionAprendiz;
    }

    public Sesion.Inasistente getInasistente() {
        return inasistente;
    }

    public void setInasistente(Sesion.Inasistente inasistente) {
        this.inasistente = inasistente;
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(OffsetDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }
}
