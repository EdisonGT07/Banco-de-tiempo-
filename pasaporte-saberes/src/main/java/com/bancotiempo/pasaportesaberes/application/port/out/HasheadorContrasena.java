package com.bancotiempo.pasaportesaberes.application.port.out;

public interface HasheadorContrasena {
    String hashear(String contrasena);

    boolean coincide(String contrasena, String hash);
}
