package com.tcc.sspsp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tcc.sspsp.dto.DelegaciasDTO;
import com.tcc.sspsp.service.DelegaciasService;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(DelegaciasController.class)
class DelegaciasControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DelegaciasService service;

	// ---------- GET /v1/delegacias ----------

	@Test
	void listar_deveRetornar200ComEnvelope() throws Exception {
		when(service.listarComFiltros(null, null)).thenReturn(List.of(new DelegaciasDTO(1L, "1º DP", "sul")));

		mockMvc.perform(get("/v1/delegacias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].delegacia").value("1º DP"))
				.andExpect(jsonPath("$.data[0].regiao").value("sul"));
	}

	@Test
	void listar_deveRetornar200ComListaVazia_quandoSemResultados() throws Exception {
		when(service.listarComFiltros("norte", null)).thenReturn(List.of());

		mockMvc.perform(get("/v1/delegacias").param("regiao", "norte"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data").isEmpty());
	}

	// ---------- GET /v1/delegacias/{id} ----------

	@Test
	void buscarPorId_deveRetornar200ComEnvelope() throws Exception {
		when(service.buscarPorId(1L)).thenReturn(new DelegaciasDTO(1L, "1º DP", "sul"));

		mockMvc.perform(get("/v1/delegacias/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.delegacia").value("1º DP"));
	}

	@Test
	void buscarPorId_deveRetornar404_quandoNaoEncontrada() throws Exception {
		when(service.buscarPorId(99L)).thenThrow(new EntityNotFoundException("Delegacia não encontrada com id: 99"));

		mockMvc.perform(get("/v1/delegacias/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Delegacia não encontrada com id: 99"))
				.andExpect(jsonPath("$.data").doesNotExist());
	}
}
