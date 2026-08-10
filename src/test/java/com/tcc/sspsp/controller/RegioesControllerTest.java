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

import com.tcc.sspsp.service.RegioesService;

@WebMvcTest(RegioesController.class)
class RegioesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegioesService service;

	@Test
	void listar_deveRetornar200ComEnvelope() throws Exception {
		when(service.listarRegioes()).thenReturn(List.of("sul", "norte", "leste"));

		mockMvc.perform(get("/v1/regioes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.data[0]").value("sul"))
				.andExpect(jsonPath("$.data[1]").value("norte"))
				.andExpect(jsonPath("$.data[2]").value("leste"));
	}

	@Test
	void listar_deveRetornar200ComListaVazia_quandoSemRegioes() throws Exception {
		when(service.listarRegioes()).thenReturn(List.of());

		mockMvc.perform(get("/v1/regioes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data").isEmpty());
	}
}
