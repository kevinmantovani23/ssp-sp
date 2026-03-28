package com.tcc.sspsp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;
import com.tcc.sspsp.repository.OcorrenciaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportacaoService {
	
	private final DelegaciasRepository delegaciasRepository;
	private final NaturezaRepository naturezaRepository;
	private final OcorrenciaRepository ocorrenciaRepository;
	
	//Primeiro, realizar uma busca em nosso banco para ver o último mês/ano que foi salvo, e o mês/ano atual
	public List<YearMonth> obterMesesPendentes() {
		LocalDate ultimaData = ocorrenciaRepository.buscarUltimaDataImportada();
		
		YearMonth mesAtual = YearMonth.now();
		
		// Se banco vazio → decide de onde começar
		if (ultimaData == null) {
		    return List.of(mesAtual);
		}
		
		YearMonth ultimoMesImportado = YearMonth.from(ultimaData);
		
		List<YearMonth> mesesPendentes = new ArrayList<>();
		
		YearMonth mes = ultimoMesImportado.plusMonths(1);
		
		while (!mes.isAfter(mesAtual)) {
		    mesesPendentes.add(mes);
		    mes = mes.plusMonths(1);
		}
		
		return mesesPendentes;
 }

	
}
