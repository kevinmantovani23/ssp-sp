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

import com.tcc.sspsp.dto.NaturezaDTO;
import com.tcc.sspsp.service.NaturezaService;

@WebMvcTest(NaturezaController.class)
class NaturezaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NaturezaService naturezaService;

	@Test
	void listar_deveRetornar200ComEnvelope() throws Exception {
		when(naturezaService.listarNaturezas()).thenReturn(List.of(new NaturezaDTO(1L, "ROUBO"), new NaturezaDTO(2L, "FURTO")));

		mockMvc.perform(get("/v1/naturezas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.data[0].id").value(1))
				.andExpect(jsonPath("$.data[0].natureza").value("ROUBO"))
				.andExpect(jsonPath("$.data[1].natureza").value("FURTO"));
	}

	@Test
	void listar_deveRetornar200ComListaVazia_quandoSemNaturezas() throws Exception {
		when(naturezaService.listarNaturezas()).thenReturn(List.of());

		mockMvc.perform(get("/v1/naturezas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data").isEmpty());
	}
}
