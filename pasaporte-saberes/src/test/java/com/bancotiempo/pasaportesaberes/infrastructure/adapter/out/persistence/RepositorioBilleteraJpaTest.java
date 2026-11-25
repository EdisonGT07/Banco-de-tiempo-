package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositorioBilleteraJpaTest {
    private final BilleteraJpaRepository billeteras = mock(BilleteraJpaRepository.class);
    private final RepositorioBilleteraJpa repositorio =
            new RepositorioBilleteraJpa(billeteras, mock(RepositorioUsuario.class));

    @Test
    void bloqueaIdsUnicosEnOrdenAscendente() {
        BilleteraJpaEntity emisor = entidad(2L, Billetera.TipoBilletera.EMISOR);
        BilleteraJpaEntity fondo = entidad(7L, Billetera.TipoBilletera.FONDO);
        when(billeteras.findByIdInOrderByIdAsc(List.of(2L, 7L)))
                .thenReturn(List.of(emisor, fondo));

        List<Billetera> bloqueadas = repositorio.bloquearPorIds(List.of(7L, 2L, 7L));

        assertEquals(List.of(2L, 7L), bloqueadas.stream().map(Billetera::getId).toList());
        verify(billeteras).findByIdInOrderByIdAsc(List.of(2L, 7L));
    }

    @Test
    void fallaSiNoEncuentraTodasLasBilleterasSolicitadas() {
        BilleteraJpaEntity emisor = entidad(2L, Billetera.TipoBilletera.EMISOR);
        when(billeteras.findByIdInOrderByIdAsc(List.of(2L, 7L)))
                .thenReturn(List.of(emisor));

        assertThrows(
                IllegalStateException.class,
                () -> repositorio.bloquearPorIds(List.of(2L, 7L))
        );
    }

    private static BilleteraJpaEntity entidad(Long id, Billetera.TipoBilletera tipo) {
        BilleteraJpaEntity entidad = mock(BilleteraJpaEntity.class);
        when(entidad.getId()).thenReturn(id);
        when(entidad.getTipo()).thenReturn(tipo);
        when(entidad.getSaldoDisponible()).thenReturn(BigDecimal.ZERO.setScale(2));
        when(entidad.getSaldoReservado()).thenReturn(BigDecimal.ZERO.setScale(2));
        when(entidad.getVersion()).thenReturn(0L);
        when(entidad.getPersonaId()).thenReturn(null);
        return entidad;
    }
}
