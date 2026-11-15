package com.empresa.reembolsos.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudInvalidaException;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import com.empresa.reembolsos.infraestructura.SolicitudReembolsoMapper;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.empresa.reembolsos.procesamiento.validacion.CadenaValidadores;

@Service
public class SolicitudReembolsoService {
    private final CadenaValidadores validadores;
    private final SelectorCalculadora calculadoras;
    private final List<ObservadorCambioEstado> observadores;
    private final RepositorioSolicitudesJpa repositorio;
    private final SolicitudReembolsoMapper mapper;

    public SolicitudReembolsoService(CadenaValidadores validadores, SelectorCalculadora calculadoras,
                                      List<ObservadorCambioEstado> observadores, RepositorioSolicitudesJpa repositorio,
                                      SolicitudReembolsoMapper mapper) {
        this.validadores = validadores;
        this.calculadoras = calculadoras;
        this.observadores = observadores;
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    public SolicitudReembolso crear(SolicitudReembolso solicitud) {
        var pendientes = repositorio.findByEmpleadoIdAndEstado(solicitud.getEmpleadoId(), EstadoSolicitud.PENDIENTE)
            .stream()
            .map(mapper::toDomain)
            .toList();
        var resultado = validadores.validar(solicitud, pendientes);
        if (!resultado.valida()) {
            throw new SolicitudInvalidaException(resultado.motivoRechazo());
        }
        solicitud.setMontoAprobado(calculadoras.obtener(solicitud.getCategoria()).calcular(solicitud));
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        var guardada = repositorio.save(mapper.toEntity(solicitud));
        return mapper.toDomain(guardada);
    }

    public SolicitudReembolso cambiarEstado(Long id, EstadoSolicitud nuevoEstado) throws Throwable {
        var solicitudJpa = repositorio.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
        var solicitud = mapper.toDomain(solicitudJpa);
        var estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);
        var guardada = mapper.toDomain(repositorio.save(mapper.toEntity(solicitud)));
        observadores.forEach(o -> o.onCambioEstado(guardada, estadoAnterior));
        return guardada;
    }
}

