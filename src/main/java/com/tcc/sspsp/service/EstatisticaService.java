package com.tcc.sspsp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.tcc.sspsp.dto.FiltroConsultaDTO;
import com.tcc.sspsp.dto.MediaOcorrenciasDTO;
import com.tcc.sspsp.repository.DelegaciasRepository;
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
	private final DelegaciasRepository delegaciasRepository;

	public MediaOcorrenciasDTO calcularMediaMensal(Long naturezaId, int ano, Long delegaciaId, String regiao) {
		regiao = NormalizaCampos.normalizaRegiao(regiao);
		if(delegaciaId != null && regiao != null){
			throw new IllegalArgumentException("Não é possível filtrar por Delegacia e Região, utilize apenas um.");
		}
		naturezaRepository.findById(naturezaId)
				.orElseThrow(() -> new EntityNotFoundException("Natureza não encontrada com id: " + naturezaId));
		validarDelegaciaExiste(delegaciaId);
		Double mediaMensal = ocorrenciaRepository.calcularMediaMensal(naturezaId, ano, delegaciaId, regiao);
		return new MediaOcorrenciasDTO(naturezaId, mediaMensal);
	}

	private void validarDelegaciaExiste(Long delegaciaId) {
		if (delegaciaId == null) return;
		if (!delegaciasRepository.existsById(delegaciaId)) {
			throw new EntityNotFoundException("Delegacia não encontrada com id: " + delegaciaId);
		}
	}

	private record ContextoAnalise(Natureza natureza, String regiao, LocalDate dataMin, LocalDate dataMax) {}


	private Optional<ContextoAnalise> prepararContexto(FiltroConsultaDTO filtro) {
		Natureza naturezaEntity = naturezaRepository.findById(filtro.naturezaId())
				.orElseThrow(() -> new EntityNotFoundException("Natureza não encontrada com id: " + filtro.naturezaId()));

		validarDelegaciaExiste(filtro.delegaciaId());

		List<Object[]> periodoList = ocorrenciaRepository.buscarPeriodoOcorrencia(filtro.naturezaId(), filtro.delegaciaId(), filtro.regiao());

		if(periodoList.getFirst()[1] == null){
			return Optional.empty();
		}

		LocalDate dataMax = (LocalDate) periodoList.getFirst()[1];

		//Média de 4 anos para cá
		LocalDate dataMin = dataMax.minusYears(4);

		return Optional.of(new ContextoAnalise(naturezaEntity, filtro.regiao(), dataMin, dataMax));
	}

	// Regressão Linear de previsão de ocorrências
	// usando historico de 4 anos
	public PrevisaoResumoDTO calcularPrevisao(FiltroConsultaDTO filtro) {

		Optional<ContextoAnalise> contexto = prepararContexto(filtro);
		if (contexto.isEmpty()) {
			return PrevisaoResumoDTO.semDados(nomeNatureza(filtro.naturezaId()));
		}
		ContextoAnalise ctx = contexto.get();

		List<Object[]> serie = ocorrenciaRepository.serieHistorica(filtro.naturezaId(), ctx.dataMin().getYear(), ctx.dataMax().getYear(), filtro.delegaciaId(), ctx.regiao());

		SimpleRegression regression = new SimpleRegression();
		for (Object[] linha : serie) {
			int ano = ((Number) linha[0]).intValue();
			int mes = ((Number) linha[1]).intValue();
			long total = ((Number) linha[2]).longValue();
			regression.addData(ano * 12 + mes, total);
		}

		YearMonth ultimoMes = YearMonth.from(ctx.dataMax());
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
		String periodoUtilizado = YearMonth.from(ctx.dataMin()).format(fmt) + " até " + ultimoMes.format(fmt);

		return new PrevisaoResumoDTO(ctx.natureza().getNatureza(), periodoUtilizado, previsao, tendencia);
	}

	// Tendencia de ocorrencias
	public TendenciaOcorrenciaDTO calcularTendencia(FiltroConsultaDTO filtro) {

		Optional<ContextoAnalise> contexto = prepararContexto(filtro);
		if (contexto.isEmpty()) {
			return TendenciaOcorrenciaDTO.semDados();
		}
		ContextoAnalise ctx = contexto.get();

		List<Object[]> serie = ocorrenciaRepository.serieAnual(filtro.naturezaId(), filtro.delegaciaId(), ctx.regiao(), ctx.dataMin().getYear(), ctx.dataMax().getYear());

		SimpleRegression regression = new SimpleRegression();

		if (serie.size() < 2){
			return new TendenciaOcorrenciaDTO("Indeterminado", null);
		}

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

	private String nomeNatureza(Long naturezaId) {
		return naturezaRepository.findById(naturezaId)
				.map(Natureza::getNatureza)
				.orElseThrow(() -> new EntityNotFoundException("Natureza não encontrada com id: " + naturezaId));
	}
}