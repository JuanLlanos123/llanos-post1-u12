package com.empresa.reembolsos.dominio;
public class SolicitudReembolso {
    private Long id;
    private String empleadoId;
    private CategoriaGasto categoria;
    private BigDecimal montoSolicitado;
    private BigDecimal montoAprobado;
    private LocalDate fechaGasto;
    private int diasViaje;
    private boolean documentosSoporte;
    private EstadoSolicitud estado;

    // getters y setters
}



