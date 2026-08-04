package com.tcc.sspsp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tcc.sspsp.dto.CoberturaDadosDTO;
import com.tcc.sspsp.dto.RankingDelegaciaDTO;
import com.tcc.sspsp.dto.SerieHistoricaDTO;
import com.tcc.sspsp.dto.TotalNaturezaDTO;
import com.tcc.sspsp.dto.TotalRegiaoDTO;
import com.tcc.sspsp.repository.OcorrenciaRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class OcorrenciaServiceTest {

	@Mock
	private OcorrenciaRepository repo;

	@InjectMocks
	private OcorrenciaService ocorrenciaService;

	// ---------- totalPorNatureza ----------

	@Test
	void totalPorNatureza_deveMapearObjectArrayParaDTO() {
		when(repo.totalPorNaturezaEAno(2024)).thenReturn(List.of(
				new Object[] { "ROUBO", 2024, 100L },
				new Object[] { "FURTO", 2024, 50L }));

		List<TotalNaturezaDTO> resultado = ocorrenciaService.totalPorNatureza(2024);

		assertEquals(2, resultado.size());
		assertEquals(new TotalNaturezaDTO("ROUBO", 2024, 100L), resultado.get(0));
		assertEquals(new TotalNaturezaDTO("FURTO", 2024, 50L), resultado.get(1));
	}

	// ---------- serieHistorica ----------

	@Test
	void serieHistorica_deveMapearObjectArrayParaDTO() {
		when(repo.serieHistorica(1L, 2020, 2024, null, null)).thenReturn(List.of(
				new Object[] { 2024, 1, 100L },
				new Object[] { 2024, 2, 110L }));

		List<SerieHistoricaDTO> resultado = ocorrenciaService.serieHistorica(1L, 2020, 2024, null, null);

		assertEquals(2, resultado.size());
		assertEquals(new SerieHistoricaDTO(2024, 1, 100L), resultado.get(0));
		assertEquals(new SerieHistoricaDTO(2024, 2, 110L), resultado.get(1));
	}

	// ---------- rankingDelegacias ----------

	@Test
	void rankingDelegacias_deveMapearObjectArrayParaDTO() {
		when(repo.rankingDelegacias(2024, 1L)).thenReturn(List.of(
				new Object[] { "1º DP", "sul", 30L },
				new Object[] { "2º DP", "norte", 20L }));

		List<RankingDelegaciaDTO> resultado = ocorrenciaService.rankingDelegacias(2024, 1L);

		assertEquals(2, resultado.size());
		assertEquals(new RankingDelegaciaDTO("1º DP", "sul", 30L), resultado.get(0));
		assertEquals(new RankingDelegaciaDTO("2º DP", "norte", 20L), resultado.get(1));
	}

	@Test
	void rankingDelegacias_deveMapearLinhaComRegiaoNula() {
		// Delegacias.regiao é uma coluna opcional no banco, então uma linha
		// real pode chegar com região nula.
		when(repo.rankingDelegacias(2024, 1L)).thenReturn(List.<Object[]>of(
				new Object[] { "1º DP", null, 30L }));

		List<RankingDelegaciaDTO> resultado = ocorrenciaService.rankingDelegacias(2024, 1L);

		assertEquals(1, resultado.size());
		assertEquals("1º DP", resultado.get(0).delegacia());
		assertNull(resultado.get(0).regiao());
		assertEquals(30L, resultado.get(0).total());
	}

	// ---------- totalPorRegiao ----------

	@Test
	void totalPorRegiao_deveMapearObjectArrayParaDTO() {
		when(repo.totalPorRegiao(2024, 1L)).thenReturn(List.of(
				new Object[] { "ROUBO", "sul", 30L },
				new Object[] { "ROUBO", "norte", 20L }));

		List<TotalRegiaoDTO> resultado = ocorrenciaService.totalPorRegiao(2024, 1L);

		assertEquals(2, resultado.size());
		assertEquals(new TotalRegiaoDTO("ROUBO", "sul", 30L), resultado.get(0));
		assertEquals(new TotalRegiaoDTO("ROUBO", "norte", 20L), resultado.get(1));
	}

	@Test
	void totalPorRegiao_deveMapearLinhaComRegiaoNula() {
		when(repo.totalPorRegiao(2024, 1L)).thenReturn(List.<Object[]>of(
				new Object[] { "ROUBO", null, 30L }));

		List<TotalRegiaoDTO> resultado = ocorrenciaService.totalPorRegiao(2024, 1L);

		assertEquals(1, resultado.size());
		assertEquals("ROUBO", resultado.get(0).natureza());
		assertNull(resultado.get(0).regiao());
		assertEquals(30L, resultado.get(0).total());
	}

	// ---------- buscarCobertura ----------

	@Test
	void buscarCobertura_deveMapearPeriodoETotal() {
		LocalDate dataMin = LocalDate.of(2020, 3, 15);
		LocalDate dataMax = LocalDate.of(2024, 6, 30);
		when(repo.buscarPeriodoGeral()).thenReturn(List.<Object[]>of(new Object[] { dataMin, dataMax }));
		when(repo.somaTotalQuantidade()).thenReturn(500L);

		CoberturaDadosDTO resultado = ocorrenciaService.buscarCobertura();

		assertEquals("2020-03", resultado.primeiroMesDisponivel());
		assertEquals("2024-06", resultado.ultimoMesDisponivel());
		assertEquals(500L, resultado.totalOcorrenciasRegistradas());
	}

	@Test
	void buscarCobertura_deveLancarEntityNotFoundException_quandoBaseVazia() {
		when(repo.buscarPeriodoGeral()).thenReturn(List.<Object[]>of(new Object[] { null, null }));

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> ocorrenciaService.buscarCobertura());

		assertTrue(ex.getMessage().contains("Não há ocorrências registradas"));
	}
}
