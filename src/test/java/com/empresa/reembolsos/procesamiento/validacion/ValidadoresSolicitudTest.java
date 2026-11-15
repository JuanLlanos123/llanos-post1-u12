package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

class ValidadoresSolicitudTest {

    @Test
    void rechazaSolicitudSinDocumentosDeSoporte() {
        var solicitud = solicitud(CategoriaGasto.VIAJE, "100000", LocalDate.of(2026, 4, 10), false);

        var resultado = new ValidadorDocumentosSoporte().validar(solicitud, List.of());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }

    @Test
    void rechazaMontoQueSuperaElTopeDeLaCategoria() {
        var solicitud = solicitud(CategoriaGasto.CAPACITACION, "3000001", LocalDate.of(2026, 4, 10), true);

        var resultado = new ValidadorMontoMaximo().validar(solicitud, List.of());

        assertFalse(resultado.valida());
        assertEquals("Monto excede el tope de la categoria CAPACITACION", resultado.motivoRechazo());
    }

    @Test
    void rechazaSolicitudDuplicadaEnElMismoMes() {
        var fecha = LocalDate.of(2026, 4, 10);
        var solicitud = solicitud(CategoriaGasto.VIAJE, "100000", fecha, true);
        var pendiente = solicitud(CategoriaGasto.VIAJE, "50000", LocalDate.of(2026, 4, 1), true);

        var resultado = new ValidadorDuplicados().validar(solicitud, List.of(pendiente));

        assertFalse(resultado.valida());
        assertEquals(
            "Ya existe una solicitud pendiente de la misma categoria en el periodo",
            resultado.motivoRechazo());
    }

    private static SolicitudReembolso solicitud(
        CategoriaGasto categoria, String monto, LocalDate fecha, boolean documentosSoporte) {
        return new SolicitudReembolso(
            null, "empleado-1", categoria, new BigDecimal(monto), fecha, 1, documentosSoporte, null);
    }
}
