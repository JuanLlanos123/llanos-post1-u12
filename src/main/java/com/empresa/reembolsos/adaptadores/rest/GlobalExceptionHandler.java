package com.empresa.reembolsos.adaptadores.rest;


import com.empresa.reembolsos.dominio.SolicitudInvalidaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(SolicitudInvalidaException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Solicitud rechazada",
                        "mensaje", ex.getMessage()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Datos de entrada inválidos",
                        "mensaje", "Verifica el formato del JSON y los valores permitidos (VIAJE, REPRESENTACION, CAPACITACION)."
                ));
    }
}