package com.empresa.reembolsos.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

// Controlador REST — solo habla con el servicio de aplicacion
public interface RepositorioSolicitudesJpa extends JpaRepository<SolicitudReembolso,Long>{
    SolicitudReembolso findByEmpleadoIdAndEstado(String id, EstadoSolicitud solicitud); 
}