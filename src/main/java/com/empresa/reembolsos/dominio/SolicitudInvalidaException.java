package com.empresa.reembolsos.dominio;

public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String motivo){
        super("No se puede por : "+motivo);
    }
    
}
