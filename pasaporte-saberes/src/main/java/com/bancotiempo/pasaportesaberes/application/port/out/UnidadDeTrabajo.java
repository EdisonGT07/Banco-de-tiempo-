package com.bancotiempo.pasaportesaberes.application.port.out;

import java.util.function.Supplier;

public interface UnidadDeTrabajo {
    <T> T ejecutar(Supplier<T> operacion);
}
