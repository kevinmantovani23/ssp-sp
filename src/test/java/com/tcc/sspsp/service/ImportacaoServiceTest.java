package com.tcc.sspsp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tcc.sspsp.client.SSPClient;
import com.tcc.sspsp.model.Delegacias;
import com.tcc.sspsp.model.Ocorrencia;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;
import com.tcc.sspsp.repository.OcorrenciaRepository;

@ExtendWith(MockitoExtension.class)
class ImportacaoServiceTest {

	@Mock
	private DelegaciasRepository delegaciasRepository;

	@Mock
	private NaturezaRepository naturezaRepository;

	@Mock
	private OcorrenciaRepository ocorrenciaRepository;

	@Mock
	private SSPClient sspClient;

	@Captor
	private ArgumentCaptor<List<YearMonth>> mesesCaptor;

	private ImportacaoService importacaoService;

	private final YearMonth mesAtual = YearMonth.now().minusMonths(2);

	@BeforeEach
	void setUp() {
		importacaoService = new ImportacaoService(delegaciasRepository, naturezaRepository, ocorrenciaRepository, sspClient);
	}

	private Delegacias delegacia(long id, int idSSP) {
		return Delegacias.builder().id(id).idSSP(idSSP).delegacia("Delegacia " + id).build();
	}

	private List<YearMonth> sequenciaDeMeses(YearMonth inicio, YearMonth fim) {
		List<YearMonth> lista = new ArrayList<>();
		YearMonth atual = inicio;
		while (!atual.isAfter(fim)) {
			lista.add(atual);
			atual = atual.plusMonths(1);
		}
		return lista;
	}

	@Test
	void obterMesesPendentes_deveGerarDe2001AteNowMenos2_quandoBaseVazia() {
		when(delegaciasRepository.findAll()).thenReturn(List.of(delegacia(1L, 10)));
		when(ocorrenciaRepository.buscarUltimaDataImportadaPorDelegacia(1L)).thenReturn(null);
		when(sspClient.buscarOcorrencias(anyList(), eq(10))).thenReturn(List.of());

		importacaoService.importarDadosSSP();

		verify(sspClient).buscarOcorrencias(mesesCaptor.capture(), eq(10));

		List<YearMonth> esperado = sequenciaDeMeses(YearMonth.of(2001, 1), mesAtual);
		assertEquals(esperado, mesesCaptor.getValue());
	}

	@Test
	void obterMesesPendentes_deveGerarApenasMesAtual_quandoUltimaDataForMesAnterior() {
		when(delegaciasRepository.findAll()).thenReturn(List.of(delegacia(1L, 10)));
		when(ocorrenciaRepository.buscarUltimaDataImportadaPorDelegacia(1L))
				.thenReturn(mesAtual.minusMonths(1).atDay(1));
		when(sspClient.buscarOcorrencias(anyList(), eq(10))).thenReturn(List.of());

		importacaoService.importarDadosSSP();

		verify(sspClient).buscarOcorrencias(mesesCaptor.capture(), eq(10));

		assertEquals(List.of(mesAtual), mesesCaptor.getValue());
	}

	@Test
	void obterMesesPendentes_deveRetornarListaVazia_quandoJaEmDia() {
		when(delegaciasRepository.findAll()).thenReturn(List.of(delegacia(1L, 10)));
		when(ocorrenciaRepository.buscarUltimaDataImportadaPorDelegacia(1L))
				.thenReturn(mesAtual.atDay(15));

		importacaoService.importarDadosSSP();

		verify(sspClient, never()).buscarOcorrencias(any(), anyInt());
		verify(ocorrenciaRepository, never()).saveAll(any());
	}

	@Test
	void importarDadosSSP_naoDeveInterromperDemaisDelegacias_quandoUmaFalhar() {
		Delegacias delegaciaComFalha = delegacia(1L, 10);
		Delegacias delegaciaOk = delegacia(2L, 20);

		when(delegaciasRepository.findAll()).thenReturn(List.of(delegaciaComFalha, delegaciaOk));
		when(ocorrenciaRepository.buscarUltimaDataImportadaPorDelegacia(1L))
				.thenReturn(mesAtual.minusMonths(1).atDay(1));
		when(ocorrenciaRepository.buscarUltimaDataImportadaPorDelegacia(2L))
				.thenReturn(mesAtual.minusMonths(1).atDay(1));

		when(sspClient.buscarOcorrencias(List.of(mesAtual), 10))
				.thenThrow(new RuntimeException("Falha simulada na SSP"));

		Ocorrencia ocorrenciaOk = new Ocorrencia();
		when(sspClient.buscarOcorrencias(List.of(mesAtual), 20))
				.thenReturn(List.of(ocorrenciaOk));

		importacaoService.importarDadosSSP();

		verify(sspClient).buscarOcorrencias(List.of(mesAtual), 10);
		verify(sspClient).buscarOcorrencias(List.of(mesAtual), 20);
		verify(ocorrenciaRepository, times(1)).saveAll(List.of(ocorrenciaOk));
	}
}
