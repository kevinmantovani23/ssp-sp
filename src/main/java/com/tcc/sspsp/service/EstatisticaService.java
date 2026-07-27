package com.tcc.sspsp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.tcc.sspsp.dto.MediaOcorrenciasDTO;
import com.tcc.sspsp.utils.NormalizaCampos;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcc.sspsp.model.Natureza;
import com.tcc.sspsp.repository.NaturezaRepository;
import com.tcc.sspsp.repository.OcorrenciaRepository;
import com.tcc.sspsp.dto.PrevisaoResumoDTO;
import com.tcc.sspsp.dto.TendenciaOcorrenciaDTO;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstatisticaService {

	private final OcorrenciaRepository ocorrenciaRepository;
	private final NaturezaRepository naturezaRepository;

	public MediaOcorrenciasDTO calcularMediaMensal(Long naturezaId, int ano, Long delegaciaId, String regiao) {
		regiao = NormalizaCampos.normalizaRegiao(regiao);
		if(delegaciaId != null && regiao != null){
			throw new IllegalArgumentException("Não é possível filtrar por Delegacia e Região, utilize apenas um.");
		}
		naturezaRepository.findById(naturezaId)
				.orElseThrow(() -> new EntityNotFoundException("Natureza não encontrada com id: " + naturezaId));

		Double mediaMensal = ocorrenciaRepository.calcularMediaMensal(naturezaId, ano, delegaciaId, regiao);
		return new MediaOcorrenciasDTO(naturezaId, mediaMensal);
	}

	// Regressão Linear de previsão de ocorrências
	// usando historico de 4 anos
	public PrevisaoResumoDTO calcularPrevisao(Long naturezaId, Long delegaciaId, String regiao) {

		if(delegaciaId != null && regiao != null){
			throw new IllegalArgumentException("Não é possível filtrar por Delegacia e Região, utilize apenas um.");
		}

		regiao = NormalizaCampos.normalizaRegiao(regiao);

		Natureza naturezaEntity = naturezaRepository.findById(naturezaId)
				.orElseThrow(() -> new EntityNotFoundException("Natureza não encontrada com id: " + naturezaId));

		List<Object[]> periodoList = ocorrenciaRepository.buscarPeriodoOcorrencia(naturezaId, delegaciaId, regiao);

		if(periodoList.getFirst()[1] == null){
			StringBuilder msgErro = new StringBuilder("Não há ocorrências registradas para a natureza de id: " + naturezaId);
			if(delegaciaId != null){
				msgErro.append(", delegacia de id: ").append(delegaciaId);
			}
			if(regiao != null){
				msgErro.append(", regiao: ").append(regiao);
			}
			throw new EntityNotFoundException(msgErro.toString());
		}

		LocalDate dataMax = (LocalDate) periodoList.getFirst()[1];

		//Média de 4 anos para cá
		LocalDate dataMin = dataMax.minusYears(4);

		List<Object[]> serie = ocorrenciaRepository.serieHistorica(naturezaId, dataMin.getYear(), dataMax.getYear(), delegaciaId, regiao);

		SimpleRegression regression = new SimpleRegression();
		for (Object[] linha : serie) {
			int ano = ((Number) linha[0]).intValue();
			int mes = ((Number) linha[1]).intValue();
			long total = ((Number) linha[2]).longValue();
			regression.addData(ano * 12 + mes, total);
		}

		YearMonth ultimoMes = YearMonth.from(dataMax);
		YearMonth proximoMes = ultimoMes.plusMonths(1);
		int proximoIndice = proximoMes.getYear() * 12 + proximoMes.getMonthValue();

		Long previsao = Math.round(regression.predict(proximoIndice));

		Double slope = regression.getSlope();
		String tendencia;
		if (slope > 0) {
			tendencia = "CRESCIMENTO";
		} else if (slope == 0) {
			tendencia = "ESTAVEL";
		} else {
			tendencia = "QUEDA";
		}

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
		String periodoUtilizado = YearMonth.from(dataMin).format(fmt) + " até " + ultimoMes.format(fmt);

		return new PrevisaoResumoDTO(naturezaEntity.getNatureza(), periodoUtilizado, previsao, tendencia);
	}

	// Tendencia de ocorrencias
	public TendenciaOcorrenciaDTO calcularTendencia(Long naturezaId, Long delegaciaId, String regiao) {

		if(delegaciaId != null && regiao != null){
			throw new IllegalArgumentException("Não é possível filtrar por Delegacia e Região, utilize apenas um.");
		}

		regiao = NormalizaCampos.normalizaRegiao(regiao);

		naturezaRepository.findById(naturezaId)
				.orElseThrow(() -> new EntityNotFoundException("Natureza não encontrada com id: " + naturezaId));

		List<Object[]> periodoList = ocorrenciaRepository.buscarPeriodoOcorrencia(naturezaId, delegaciaId, regiao);

		if(periodoList.getFirst()[1] == null){
			StringBuilder msgErro = new StringBuilder("Não há ocorrências registradas para a natureza de id: " + naturezaId);
			if(delegaciaId != null){
				msgErro.append(", delegacia de id: ").append(delegaciaId);
			}
			if(regiao != null){
				msgErro.append(", regiao: ").append(regiao);
			}
			throw new EntityNotFoundException(msgErro.toString());
		}

		LocalDate dataMax = (LocalDate) periodoList.getFirst()[1];

		//Média de 4 anos para cá
		LocalDate dataMin  = dataMax.minusYears(4);

		List<Object[]> serie = ocorrenciaRepository.serieAnual(naturezaId, delegaciaId, regiao, dataMin.getYear(), dataMax.getYear());

		SimpleRegression regression = new SimpleRegression();

		for (Object[] linha : serie) {
			int ano = ((Number) linha[0]).intValue();
			long total = ((Number) linha[1]).longValue();
			regression.addData(ano, total);
		}

		Double previsao = regression.getSlope();
		String tendencia;
		if (previsao > 0) {
			tendencia = "Crescimento";
		} else if (previsao == 0) {
			tendencia = "Estável";
		} else
			tendencia = "Queda";

		return new TendenciaOcorrenciaDTO(tendencia, previsao);

	}
}