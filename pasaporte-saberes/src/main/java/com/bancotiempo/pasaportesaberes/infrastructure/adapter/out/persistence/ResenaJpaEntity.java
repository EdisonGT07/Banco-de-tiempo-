package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "resena")
public class ResenaJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_id", nullable = false)
    private Long sesionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "autor_rol", nullable = false, length = 10)
    private Resena.AutorRol autorRol;

    @Column(name = "calificacion", nullable = false)
    private Short calificacion;

    @Column(name = "comentario")
    private String comentario;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    protected ResenaJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public Resena.AutorRol getAutorRol() {
        return autorRol;
    }

    public void setAutorRol(Resena.AutorRol autorRol) {
        this.autorRol = autorRol;
    }

    public Short getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Short calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(OffsetDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }
}
