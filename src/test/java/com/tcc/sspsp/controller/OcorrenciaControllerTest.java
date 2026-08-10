package com.tcc.sspsp.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tcc.sspsp.dto.CoberturaDadosDTO;
import com.tcc.sspsp.dto.OcorrenciaFiltroDTO;
import com.tcc.sspsp.dto.OcorrenciaResponseDTO;
import com.tcc.sspsp.dto.RankingDelegaciaDTO;
import com.tcc.sspsp.dto.SerieHistoricaDTO;
import com.tcc.sspsp.dto.TotalNaturezaDTO;
import com.tcc.sspsp.dto.TotalRegiaoDTO;
import com.tcc.sspsp.service.OcorrenciaService;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(OcorrenciaController.class)
class OcorrenciaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OcorrenciaService service;

	// ---------- GET /v1/ocorrencias ----------

	@Test
	void listar_deveRetornar200ComEnvelopeDePagina() throws Exception {
		OcorrenciaResponseDTO ocorrencia = new OcorrenciaResponseDTO(1L, "ROUBO", "violento", "1º DP", "sul", 10, LocalDate.of(2024, 1, 1));
		when(service.listarComFiltros(new OcorrenciaFiltroDTO(2024, 1L, null, 0, 20)))
				.thenReturn(new PageImpl<>(List.of(ocorrencia), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/v1/ocorrencias").param("ano", "2024").param("naturezaId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content[0].id").value(1))
				.andExpect(jsonPath("$.data.content[0].natureza").value("ROUBO"))
				.andExpect(jsonPath("$.data.totalElements").value(1));
	}

	// ---------- GET /v1/ocorrencias/totais ----------

	@Test
	void totaisPorNatureza_deveRetornar200ComEnvelope() throws Exception {
		when(service.totalPorNatureza(2024)).thenReturn(List.of(new TotalNaturezaDTO("ROUBO", 2024, 100L)));

		mockMvc.perform(get("/v1/ocorrencias/totais").param("ano", "2024"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].natureza").value("ROUBO"))
				.andExpect(jsonPath("$.data[0].total").value(100));
	}

	// ---------- GET /v1/ocorrencias/serie-historica ----------

	@Test
	void serieHistorica_deveRetornar200ComEnvelope() throws Exception {
		when(service.serieHistorica(1L, 2020, 2024, null, null))
				.thenReturn(List.of(new SerieHistoricaDTO(2024, 1, 100L)));

		mockMvc.perform(get("/v1/ocorrencias/serie-historica").param("naturezaId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].ano").value(2024))
				.andExpect(jsonPath("$.data[0].mes").value(1))
				.andExpect(jsonPath("$.data[0].total").value(100));
	}

	@Test
	void serieHistorica_deveRetornar400_quandoDelegaciaERegiaoInformadas() throws Exception {
		when(service.serieHistorica(eq(1L), eq(2020), eq(2024), eq(5L), eq("sul")))
				.thenThrow(new IllegalArgumentException("Não é possível filtrar por Delegacia e Região, utilize apenas um."));

		mockMvc.perform(get("/v1/ocorrencias/serie-historica")
						.param("naturezaId", "1").param("delegaciaId", "5").param("regiao", "sul"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Não é possível filtrar por Delegacia e Região, utilize apenas um."));
	}

	// ---------- GET /v1/ocorrencias/ranking-delegacias ----------

	@Test
	void rankingDelegacias_deveRetornar200ComEnvelope() throws Exception {
		when(service.rankingDelegacias(2024, null)).thenReturn(List.of(new RankingDelegaciaDTO("1º DP", "sul", 30L)));

		mockMvc.perform(get("/v1/ocorrencias/ranking-delegacias").param("ano", "2024"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].delegacia").value("1º DP"))
				.andExpect(jsonPath("$.data[0].total").value(30));
	}

	// ---------- GET /v1/ocorrencias/totais-por-regiao ----------

	@Test
	void totaisPorRegiao_deveRetornar200ComEnvelope() throws Exception {
		when(service.totalPorRegiao(2024, null)).thenReturn(List.of(new TotalRegiaoDTO("ROUBO", "sul", 30L)));

		mockMvc.perform(get("/v1/ocorrencias/totais-por-regiao").param("ano", "2024"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].regiao").value("sul"));
	}

	// ---------- GET /v1/ocorrencias/cobertura ----------

	@Test
	void cobertura_deveRetornar200ComEnvelope() throws Exception {
		when(service.buscarCobertura()).thenReturn(new CoberturaDadosDTO("2020-01", "2024-06", 5000L));

		mockMvc.perform(get("/v1/ocorrencias/cobertura"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.primeiroMesDisponivel").value("2020-01"))
				.andExpect(jsonPath("$.data.ultimoMesDisponivel").value("2024-06"))
				.andExpect(jsonPath("$.data.totalOcorrenciasRegistradas").value(5000));
	}

	@Test
	void cobertura_deveRetornar404_quandoBaseVazia() throws Exception {
		when(service.buscarCobertura()).thenThrow(new EntityNotFoundException("Não há ocorrências registradas na base."));

		mockMvc.perform(get("/v1/ocorrencias/cobertura"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Não há ocorrências registradas na base."));
	}
}
