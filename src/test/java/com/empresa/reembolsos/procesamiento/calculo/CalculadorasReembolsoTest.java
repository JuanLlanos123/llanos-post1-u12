package com.empresa.reembolsos.procesamiento.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

class CalculadorasReembolsoTest {

    @Test
    void limitaReembolsoDeViajeAlTopePorDias() {
        var solicitud = solicitud(CategoriaGasto.VIAJE, "500000", 2);

        var resultado = new CalculadoraViaje().calcular(solicitud);

        assertEquals(0, new BigDecimal("300000").compareTo(resultado));
    }

    @Test
    void limitaReembolsoDeRepresentacionAlTopeMensual() {
        var solicitud = solicitud(CategoriaGasto.REPRESENTACION, "2500000", 0);

        var resultado = new CalculadoraRepresentacion().calcular(solicitud);

        assertEquals(0, new BigDecimal("2000000").compareTo(resultado));
    }

    @Test
    void limitaReembolsoDeCapacitacionAlTopeAnual() {
        var solicitud = solicitud(CategoriaGasto.CAPACITACION, "5000000", 0);

        var resultado = new CalculadoraCapacitacion().calcular(solicitud);

        assertEquals(0, new BigDecimal("3000000").compareTo(resultado));
    }

    private static SolicitudReembolso solicitud(CategoriaGasto categoria, String monto, int diasViaje) {
        return new SolicitudReembolso(
            null, "empleado-1", categoria, new BigDecimal(monto), LocalDate.of(2026, 4, 10),
            diasViaje, true, null);
    }
}
