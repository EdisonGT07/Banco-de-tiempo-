package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "rango")
public class RangoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "escala", nullable = false, length = 12)
    private Rango.EscalaRango escala;

    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Short orden;

    @Column(name = "min_horas", nullable = false, precision = 8, scale = 2)
    private BigDecimal minimoHoras;

    @Column(name = "min_rating", precision = 3, scale = 2)
    private BigDecimal minimoRating;

    @Column(name = "min_aprendices")
    private Integer minimoAprendices;

    @Column(name = "min_habilidades")
    private Integer minimoHabilidades;

    @Column(name = "min_asistencia", precision = 4, scale = 3)
    private BigDecimal minimoAsistencia;

    protected RangoJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rango.EscalaRango getEscala() {
        return escala;
    }

    public void setEscala(Rango.EscalaRango escala) {
        this.escala = escala;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Short getOrden() {
        return orden;
    }

    public void setOrden(Short orden) {
        this.orden = orden;
    }

    public BigDecimal getMinimoHoras() {
        return minimoHoras;
    }

    public void setMinimoHoras(BigDecimal minimoHoras) {
        this.minimoHoras = minimoHoras;
    }

    public BigDecimal getMinimoRating() {
        return minimoRating;
    }

    public void setMinimoRating(BigDecimal minimoRating) {
        this.minimoRating = minimoRating;
    }

    public Integer getMinimoAprendices() {
        return minimoAprendices;
    }

    public void setMinimoAprendices(Integer minimoAprendices) {
        this.minimoAprendices = minimoAprendices;
    }

    public Integer getMinimoHabilidades() {
        return minimoHabilidades;
    }

    public void setMinimoHabilidades(Integer minimoHabilidades) {
        this.minimoHabilidades = minimoHabilidades;
    }

    public BigDecimal getMinimoAsistencia() {
        return minimoAsistencia;
    }

    public void setMinimoAsistencia(BigDecimal minimoAsistencia) {
        this.minimoAsistencia = minimoAsistencia;
    }
}
