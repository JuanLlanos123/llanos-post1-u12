package com.empresa.reembolsos.infraestructura;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioSolicitudesJpa extends JpaRepository<SolicitudReembolsoJpa, Long> {
       List<SolicitudReembolsoJpa> findByEmpleadoIdAndEstado(String empleadoId, EstadoSolicitud estado);
}
