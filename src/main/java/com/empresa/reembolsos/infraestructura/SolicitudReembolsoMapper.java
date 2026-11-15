package com.empresa.reembolsos.infraestructura;

import org.springframework.stereotype.Component;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

@Component
public class SolicitudReembolsoMapper {

    public SolicitudReembolsoJpa toEntity(SolicitudReembolso solicitud) {
        var entidad = new SolicitudReembolsoJpa();
        entidad.setId(solicitud.getId());
        entidad.setEmpleadoId(solicitud.getEmpleadoId());
        entidad.setCategoria(solicitud.getCategoria());
        entidad.setMontoSolicitado(solicitud.getMontoSolicitado());
        entidad.setMontoAprobado(solicitud.getMontoAprobado());
        entidad.setFechaGasto(solicitud.getFechaGasto());
        entidad.setDiasViaje(solicitud.getDiasViaje());
        entidad.setDocumentosSoporte(solicitud.isDocumentosSoporte());
        entidad.setEstado(solicitud.getEstado());
        return entidad;
    }

    public SolicitudReembolso toDomain(SolicitudReembolsoJpa entidad) {
        var solicitud = new SolicitudReembolso();
        solicitud.setId(entidad.getId());
        solicitud.setEmpleadoId(entidad.getEmpleadoId());
        solicitud.setCategoria(entidad.getCategoria());
        solicitud.setMontoSolicitado(entidad.getMontoSolicitado());
        solicitud.setMontoAprobado(entidad.getMontoAprobado());
        solicitud.setFechaGasto(entidad.getFechaGasto());
        solicitud.setDiasViaje(entidad.getDiasViaje());
        solicitud.setDocumentosSoporte(entidad.isDocumentosSoporte());
        solicitud.setEstado(entidad.getEstado());
        return solicitud;
    }
}
