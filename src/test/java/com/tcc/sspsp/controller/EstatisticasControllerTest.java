package com.tcc.sspsp.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tcc.sspsp.dto.FiltroConsultaDTO;
import com.tcc.sspsp.dto.MediaOcorrenciasDTO;
import com.tcc.sspsp.dto.PrevisaoResumoDTO;
import com.tcc.sspsp.dto.TendenciaOcorrenciaDTO;
import com.tcc.sspsp.service.EstatisticaService;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(EstatisticasController.class)
class EstatisticasControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EstatisticaService service;

	// ---------- /media-mensal ----------

	@Test
	void mediaMensal_deveRetornar200ComEnvelope() throws Exception {
		when(service.calcularMediaMensal(1L, 2024, null, null))
				.thenReturn(new MediaOcorrenciasDTO(1L, 12.5));

		mockMvc.perform(get("/v1/estatisticas/media-mensal").param("naturezaId", "1").param("ano", "2024"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.data.naturezaId").value(1))
				.andExpect(jsonPath("$.data.mediaMensal").value(12.5));
	}

	@Test
	void mediaMensal_deveRetornar400_quandoDelegaciaERegiaoInformadas() throws Exception {
		when(service.calcularMediaMensal(eq(1L), eq(2024), eq(5L), eq("sul")))
				.thenThrow(new IllegalArgumentException("Não é possível filtrar por Delegacia e Região, utilize apenas um."));

		mockMvc.perform(get("/v1/estatisticas/media-mensal")
						.param("naturezaId", "1").param("ano", "2024").param("delegaciaId", "5").param("regiao", "sul"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Não é possível filtrar por Delegacia e Região, utilize apenas um."))
				.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void mediaMensal_deveRetornar404_quandoNaturezaNaoEncontrada() throws Exception {
		when(service.calcularMediaMensal(eq(99L), anyInt(), isNull(), isNull()))
				.thenThrow(new EntityNotFoundException("Natureza não encontrada com id: 99"));

		mockMvc.perform(get("/v1/estatisticas/media-mensal").param("naturezaId", "99").param("ano", "2024"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Natureza não encontrada com id: 99"));
	}

	@Test
	void mediaMensal_deveRetornar400_quandoNaturezaIdAusente() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/media-mensal").param("ano", "2024"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Parâmetro obrigatório ausente: naturezaId"));
	}

	@Test
	void mediaMensal_deveRetornar400_quandoAnoComTipoInvalido() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/media-mensal").param("naturezaId", "1").param("ano", "não-é-um-ano"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Parâmetro 'ano' com valor inválido: não-é-um-ano"));
	}

	@Test
	void mediaMensal_deveRetornar400_quandoAnoAntesDe2001() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/media-mensal").param("naturezaId", "1").param("ano", "2000"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("mediaMensal.ano: must be greater than or equal to 2001"));
	}

	@Test
	void mediaMensal_deveRetornar400_quandoAnoDepoisDe2100() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/media-mensal").param("naturezaId", "1").param("ano", "2101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("mediaMensal.ano: must be less than or equal to 2100"));
	}

	// ---------- /previsao ----------

	@Test
	void previsao_deveRetornar200ComEnvelope() throws Exception {
		when(service.calcularPrevisao(new FiltroConsultaDTO(1L, null, null, null, null)))
				.thenReturn(new PrevisaoResumoDTO("ROUBO", "2020-06 até 2024-06", 160L, "CRESCIMENTO"));

		mockMvc.perform(get("/v1/estatisticas/previsao").param("naturezaId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.natureza").value("ROUBO"))
				.andExpect(jsonPath("$.data.previsao").value(160))
				.andExpect(jsonPath("$.data.tendencia").value("CRESCIMENTO"));
	}

	@Test
	void previsao_deveRetornar200ComSemDados_quandoNaoHaOcorrencias() throws Exception {
		when(service.calcularPrevisao(new FiltroConsultaDTO(1L, null, null, null, null)))
				.thenReturn(PrevisaoResumoDTO.semDados("ROUBO"));

		mockMvc.perform(get("/v1/estatisticas/previsao").param("naturezaId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.natureza").value("ROUBO"))
				.andExpect(jsonPath("$.data.tendencia").value("Sem dados"))
				.andExpect(jsonPath("$.data.previsao").doesNotExist());
	}

	@Test
	void previsao_deveRetornar404_quandoDelegaciaNaoEncontrada() throws Exception {
		when(service.calcularPrevisao(new FiltroConsultaDTO(1L, 5L, null, null, null)))
				.thenThrow(new EntityNotFoundException("Delegacia não encontrada com id: 5"));

		mockMvc.perform(get("/v1/estatisticas/previsao").param("naturezaId", "1").param("delegaciaId", "5"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Delegacia não encontrada com id: 5"));
	}

	@Test
	void previsao_deveRetornar400_quandoDelegaciaERegiaoInformadas() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/previsao")
						.param("naturezaId", "1").param("delegaciaId", "5").param("regiao", "sul"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Informe delegaciaId ou regiao, não ambos."));
	}

	@Test
	void previsao_deveRetornar400_quandoNaturezaIdAusente() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/previsao"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ---------- /tendencia ----------

	@Test
	void tendencia_deveRetornar200ComEnvelope() throws Exception {
		when(service.calcularTendencia(new FiltroConsultaDTO(1L, null, null, null, null)))
				.thenReturn(new TendenciaOcorrenciaDTO("Crescimento", 10.0));

		mockMvc.perform(get("/v1/estatisticas/tendencia").param("naturezaId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.tendencia").value("Crescimento"))
				.andExpect(jsonPath("$.data.valor").value(10.0));
	}

	@Test
	void tendencia_deveRetornar200ComSemDados_quandoNaoHaOcorrencias() throws Exception {
		when(service.calcularTendencia(new FiltroConsultaDTO(1L, null, null, null, null)))
				.thenReturn(TendenciaOcorrenciaDTO.semDados());

		mockMvc.perform(get("/v1/estatisticas/tendencia").param("naturezaId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.tendencia").value("Sem dados"))
				.andExpect(jsonPath("$.data.valor").doesNotExist());
	}

	@Test
	void tendencia_deveRetornar404_quandoDelegaciaNaoEncontrada() throws Exception {
		when(service.calcularTendencia(new FiltroConsultaDTO(1L, 5L, null, null, null)))
				.thenThrow(new EntityNotFoundException("Delegacia não encontrada com id: 5"));

		mockMvc.perform(get("/v1/estatisticas/tendencia").param("naturezaId", "1").param("delegaciaId", "5"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Delegacia não encontrada com id: 5"));
	}

	@Test
	void tendencia_deveRetornar400_quandoDelegaciaERegiaoInformadas() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/tendencia")
						.param("naturezaId", "1").param("delegaciaId", "5").param("regiao", "sul"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Informe delegaciaId ou regiao, não ambos."));
	}

	@Test
	void tendencia_deveRetornar400_quandoNaturezaIdAusente() throws Exception {
		mockMvc.perform(get("/v1/estatisticas/tendencia"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}
