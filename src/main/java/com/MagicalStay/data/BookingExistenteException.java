package com.MagicalStay.data;

public class BookingExistenteException extends Exception {

    public BookingExistenteException() {
        super("Un empleado con esta identificación ya existe");
    }

    public BookingExistenteException(String mensaje) {
        super(mensaje);

    }

}
