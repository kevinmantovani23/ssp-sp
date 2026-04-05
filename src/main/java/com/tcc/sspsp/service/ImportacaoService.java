package com.tcc.sspsp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tcc.sspsp.dto.OcorrenciaDTO;
import com.tcc.sspsp.model.Delegacias;
import com.tcc.sspsp.model.Ocorrencia;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;
import com.tcc.sspsp.repository.OcorrenciaRepository;

import client.SSPClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportacaoService {
	
	private final DelegaciasRepository delegaciasRepository;
	private final NaturezaRepository naturezaRepository;
	private final OcorrenciaRepository ocorrenciaRepository;
	private final SSPClient sspClient;
	
	//Primeiro, realizar uma busca em nosso banco para ver o último mês/ano que foi salvo, e o mês/ano atual
	//Obs: A API SSP só disponibiliza os dados de 2 meses atrás em diante
	private List<YearMonth> obterMesesPendentes() {
		LocalDate ultimaData = ocorrenciaRepository.buscarUltimaDataImportada();
		
		YearMonth mesAtual = YearMonth.now().minusMonths(2);
		
		List<YearMonth> mesesPendentes = new ArrayList<>();
		
		// Se banco estiver vazio, preencher todas as datas primeiro
		if (ultimaData == null) {
			int year = 2001;
			int month;
			
			while(year < mesAtual.getYear()) {
				for(month = 1; month <= 12; month++) {
					mesesPendentes.add(YearMonth.of(year, month));
				}
				year++;
			}
			
			for(month = 1; month <= mesAtual.getMonthValue(); month++) {
				mesesPendentes.add(YearMonth.of(year, month));
			}
			
		    return mesesPendentes;
		}
		
		YearMonth ultimoMesImportado = YearMonth.from(ultimaData);
		
		
		
		YearMonth mes = ultimoMesImportado.plusMonths(1);
		
		while (!mes.isAfter(mesAtual)) {
		    mesesPendentes.add(mes);
		    mes = mes.plusMonths(1);
		}
		
		return mesesPendentes;
	}
	
	public void importarDadosSSP() {
		List<YearMonth> meses = obterMesesPendentes();
		List<Ocorrencia> dados = new ArrayList<Ocorrencia>();
		List<Delegacias> listaDelegacias = delegaciasRepository.findAll();
		for(Delegacias delegacia : listaDelegacias) {
			
			dados = sspClient.buscarOcorrencias(meses, delegacia.getIdSSP());
			salvarNoBanco(dados);
		}
		
		
	}
	
	private void salvarNoBanco(List<Ocorrencia> ocorrencias) {
		for(Ocorrencia ocorrencia : ocorrencias) {
			ocorrenciaRepository.save(ocorrencia);
		}
	}

	
}
