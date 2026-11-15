package com.empresa.reembolsos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ReembolsosIntegradoApplicationTests {

	@Autowired
	private RepositorioSolicitudesJpa repositorio;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void limpiarSolicitudes() {
		repositorio.deleteAll();
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void creaCalculaPersisteYCambiaEstadoPorLaApi() throws Exception {
		mockMvc.perform(post("/api/solicitudes")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
					"empleadoId": "empleado-integracion",
					"categoria": "CAPACITACION",
					"montoSolicitado": 100000,
					"fechaGasto": "2026-04-10",
					"diasViaje": 0,
					"documentosSoporte": true
				}
				"""))
			.andExpect(status().isOk());

		assertEquals(1, repositorio.count());
		var creada = repositorio.findAll().get(0);
		assertNotNull(creada.getId());
		assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
		assertEquals(0, new BigDecimal("80000").compareTo(creada.getMontoAprobado()));

		mockMvc.perform(put("/api/solicitudes/{id}/estado", creada.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.content("\"APROBADA\""))
			.andExpect(status().isOk());

		var persistida = repositorio.findById(creada.getId()).orElseThrow();

		assertEquals(EstadoSolicitud.APROBADA, persistida.getEstado());
	}
}
