package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "oferta")
public class OfertaJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "habilidad_id", nullable = false)
    private Long habilidadId;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "duracion_horas", nullable = false, precision = 4, scale = 2)
    private BigDecimal duracionHoras;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false, length = 12)
    private Oferta.Modalidad modalidad;

    @Column(name = "barrio", length = 80)
    private String barrio;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino_horas", nullable = false, length = 16)
    private Oferta.DestinoHoras destinoHoras;

    @Column(name = "receptor_regalo_id")
    private Long receptorRegaloId;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    protected OfertaJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
    }

    public Long getHabilidadId() {
        return habilidadId;
    }

    public void setHabilidadId(Long habilidadId) {
        this.habilidadId = habilidadId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getDuracionHoras() {
        return duracionHoras;
    }

    public void setDuracionHoras(BigDecimal duracionHoras) {
        this.duracionHoras = duracionHoras;
    }

    public Oferta.Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Oferta.Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public Oferta.DestinoHoras getDestinoHoras() {
        return destinoHoras;
    }

    public void setDestinoHoras(Oferta.DestinoHoras destinoHoras) {
        this.destinoHoras = destinoHoras;
    }

    public Long getReceptorRegaloId() {
        return receptorRegaloId;
    }

    public void setReceptorRegaloId(Long receptorRegaloId) {
        this.receptorRegaloId = receptorRegaloId;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(OffsetDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }
}
