package com.tcc.sspsp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tcc.sspsp.dto.PrevisaoResumoDTO;
import com.tcc.sspsp.dto.TendenciaOcorrenciaDTO;
import com.tcc.sspsp.model.Natureza;
import com.tcc.sspsp.repository.NaturezaRepository;
import com.tcc.sspsp.repository.OcorrenciaRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EstatisticaServiceTest {

	@Mock
	private OcorrenciaRepository ocorrenciaRepository;

	@Mock
	private NaturezaRepository naturezaRepository;

	@InjectMocks
	private EstatisticaService estatisticaService;

	private static final Long NATUREZA_ID = 1L;

	private Natureza natureza() {
		return Natureza.builder().id(NATUREZA_ID).natureza("ROUBO").build();
	}

	private void mockarNaturezaEPeriodo(LocalDate dataMax) {
		when(naturezaRepository.findById(NATUREZA_ID)).thenReturn(Optional.of(natureza()));
		when(ocorrenciaRepository.buscarPeriodoOcorrencia(NATUREZA_ID, null, null))
				.thenReturn(List.<Object[]>of(new Object[] { dataMax.minusYears(4), dataMax }));
	}

	// ---------- calcularPrevisao ----------

	@Test
	void calcularPrevisao_deveIndicarCrescimento() {
		LocalDate dataMax = LocalDate.of(2024, 6, 30);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieHistorica(NATUREZA_ID, 2020, 2024, null, null)).thenReturn(List.of(
				new Object[] { 2024, 1, 100L },
				new Object[] { 2024, 2, 110L },
				new Object[] { 2024, 3, 120L },
				new Object[] { 2024, 4, 130L },
				new Object[] { 2024, 5, 140L },
				new Object[] { 2024, 6, 150L }));

		PrevisaoResumoDTO dto = estatisticaService.calcularPrevisao(NATUREZA_ID, null, null);

		assertEquals("CRESCIMENTO", dto.tendencia());
		assertEquals(160L, dto.previsao());
		assertEquals("2020-06 até 2024-06", dto.periodoUtilizado());
	}

	@Test
	void calcularPrevisao_deveIndicarQueda() {
		LocalDate dataMax = LocalDate.of(2024, 6, 30);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieHistorica(NATUREZA_ID, 2020, 2024, null, null)).thenReturn(List.of(
				new Object[] { 2024, 1, 150L },
				new Object[] { 2024, 2, 140L },
				new Object[] { 2024, 3, 130L },
				new Object[] { 2024, 4, 120L },
				new Object[] { 2024, 5, 110L },
				new Object[] { 2024, 6, 100L }));

		PrevisaoResumoDTO dto = estatisticaService.calcularPrevisao(NATUREZA_ID, null, null);

		assertEquals("QUEDA", dto.tendencia());
		assertEquals(90L, dto.previsao());
	}

	@Test
	void calcularPrevisao_deveIndicarEstabilidadeQuandoSerieConstante() {
		LocalDate dataMax = LocalDate.of(2024, 6, 30);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieHistorica(NATUREZA_ID, 2020, 2024, null, null)).thenReturn(List.of(
				new Object[] { 2024, 1, 100L },
				new Object[] { 2024, 2, 100L },
				new Object[] { 2024, 3, 100L },
				new Object[] { 2024, 4, 100L },
				new Object[] { 2024, 5, 100L },
				new Object[] { 2024, 6, 100L }));

		PrevisaoResumoDTO dto = estatisticaService.calcularPrevisao(NATUREZA_ID, null, null);

		assertEquals("ESTAVEL", dto.tendencia());
		assertEquals(100L, dto.previsao());
	}

	@Test
	void calcularPrevisao_deveTratarSerieComUnicoPonto() {
		LocalDate dataMax = LocalDate.of(2024, 6, 30);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieHistorica(NATUREZA_ID, 2020, 2024, null, null))
				.thenReturn(List.<Object[]>of(new Object[] { 2024, 6, 100L }));

		// com um único ponto a regressão não tem variância em X: slope = NaN,
		// e o código cai no ramo "else" (QUEDA) e Math.round(NaN) = 0.
		PrevisaoResumoDTO dto = estatisticaService.calcularPrevisao(NATUREZA_ID, null, null);

		assertEquals("QUEDA", dto.tendencia());
		assertEquals(0L, dto.previsao());
	}

	@Test
	void calcularPrevisao_deveCalcularIndiceCorretamenteNaViradaDoAno() {
		LocalDate dataMax = LocalDate.of(2023, 12, 31);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieHistorica(NATUREZA_ID, 2019, 2023, null, null)).thenReturn(List.of(
				new Object[] { 2023, 11, 100L },
				new Object[] { 2023, 12, 110L }));

		PrevisaoResumoDTO dto = estatisticaService.calcularPrevisao(NATUREZA_ID, null, null);

		assertEquals("CRESCIMENTO", dto.tendencia());
		assertEquals(120L, dto.previsao());
		assertEquals("2019-12 até 2023-12", dto.periodoUtilizado());
	}

	// ---------- calcularTendencia ----------

	@Test
	void calcularTendencia_deveIndicarCrescimento() {
		LocalDate dataMax = LocalDate.of(2023, 12, 31);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieAnual(NATUREZA_ID, null, null, 2019, 2023)).thenReturn(List.of(
				new Object[] { 2021, 100L },
				new Object[] { 2022, 110L },
				new Object[] { 2023, 120L }));

		TendenciaOcorrenciaDTO dto = estatisticaService.calcularTendencia(NATUREZA_ID, null, null);

		assertEquals("Crescimento", dto.tendencia());
		assertEquals(10.0, dto.valor());
	}

	@Test
	void calcularTendencia_deveIndicarQueda() {
		LocalDate dataMax = LocalDate.of(2023, 12, 31);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieAnual(NATUREZA_ID, null, null, 2019, 2023)).thenReturn(List.of(
				new Object[] { 2021, 120L },
				new Object[] { 2022, 110L },
				new Object[] { 2023, 100L }));

		TendenciaOcorrenciaDTO dto = estatisticaService.calcularTendencia(NATUREZA_ID, null, null);

		assertEquals("Queda", dto.tendencia());
		assertEquals(-10.0, dto.valor());
	}

	@Test
	void calcularTendencia_deveIndicarEstabilidadeQuandoSerieConstante() {
		LocalDate dataMax = LocalDate.of(2023, 12, 31);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieAnual(NATUREZA_ID, null, null, 2019, 2023)).thenReturn(List.of(
				new Object[] { 2021, 100L },
				new Object[] { 2022, 100L },
				new Object[] { 2023, 100L }));

		TendenciaOcorrenciaDTO dto = estatisticaService.calcularTendencia(NATUREZA_ID, null, null);

		assertEquals("Estável", dto.tendencia());
		assertEquals(0.0, dto.valor());
	}

	@Test
	void calcularTendencia_deveTratarSerieComUnicoPonto() {
		LocalDate dataMax = LocalDate.of(2023, 12, 31);
		mockarNaturezaEPeriodo(dataMax);
		when(ocorrenciaRepository.serieAnual(NATUREZA_ID, null, null, 2019, 2023))
				.thenReturn(List.<Object[]>of(new Object[] { 2023, 100L }));

		TendenciaOcorrenciaDTO dto = estatisticaService.calcularTendencia(NATUREZA_ID, null, null);

		assertEquals("Queda", dto.tendencia());
		assertTrue(Double.isNaN(dto.valor()));
	}

	// ---------- exclusão mútua delegacia x região ----------

	@Test
	void calcularMediaMensal_deveLancarExcecao_quandoDelegaciaERegiaoInformadas() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> estatisticaService.calcularMediaMensal(NATUREZA_ID, 2024, 5L, "sul"));

		assertTrue(ex.getMessage().contains("Delegacia e Região"));
		verifyNoInteractions(naturezaRepository, ocorrenciaRepository);
	}

	@Test
	void calcularPrevisao_deveLancarExcecao_quandoDelegaciaERegiaoInformadas() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> estatisticaService.calcularPrevisao(NATUREZA_ID, 5L, "sul"));

		assertTrue(ex.getMessage().contains("Delegacia e Região"));
		verifyNoInteractions(naturezaRepository, ocorrenciaRepository);
	}

	@Test
	void calcularTendencia_deveLancarExcecao_quandoDelegaciaERegiaoInformadas() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> estatisticaService.calcularTendencia(NATUREZA_ID, 5L, "sul"));

		assertTrue(ex.getMessage().contains("Delegacia e Região"));
		verifyNoInteractions(naturezaRepository, ocorrenciaRepository);
	}

	// ---------- natureza inexistente ----------

	@Test
	void calcularMediaMensal_deveLancarEntityNotFoundException_quandoNaturezaNaoExiste() {
		when(naturezaRepository.findById(NATUREZA_ID)).thenReturn(Optional.empty());

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> estatisticaService.calcularMediaMensal(NATUREZA_ID, 2024, null, null));

		assertTrue(ex.getMessage().contains("Natureza não encontrada"));
	}

	@Test
	void calcularPrevisao_deveLancarEntityNotFoundException_quandoNaturezaNaoExiste() {
		when(naturezaRepository.findById(NATUREZA_ID)).thenReturn(Optional.empty());

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> estatisticaService.calcularPrevisao(NATUREZA_ID, null, null));

		assertTrue(ex.getMessage().contains("Natureza não encontrada"));
	}

	@Test
	void calcularPrevisao_deveLancarEntityNotFoundException_quandoNaoHaOcorrencias() {
		when(naturezaRepository.findById(NATUREZA_ID)).thenReturn(Optional.of(natureza()));
		when(ocorrenciaRepository.buscarPeriodoOcorrencia(NATUREZA_ID, null, null))
				.thenReturn(List.<Object[]>of(new Object[] { null, null }));

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> estatisticaService.calcularPrevisao(NATUREZA_ID, null, null));

		assertTrue(ex.getMessage().contains("Não há ocorrências registradas"));
	}
}
