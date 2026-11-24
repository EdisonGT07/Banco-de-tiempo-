package com.bancotiempo.pasaportesaberes.application.port.out;

import java.math.BigDecimal;

public interface NotificadorTope {
    void notificarExcedente(Long personaId, BigDecimal horasTransferidas);
}
