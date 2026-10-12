package com.tcc.sspsp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tcc.sspsp.client.SSPClient;
import com.tcc.sspsp.model.Delegacias;
import com.tcc.sspsp.model.Ocorrencia;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;
import com.tcc.sspsp.repository.OcorrenciaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImportacaoService {

	private final DelegaciasRepository delegaciasRepository;
	private final NaturezaRepository naturezaRepository;
	private final OcorrenciaRepository ocorrenciaRepository;
	private final SSPClient sspClient;

	// Ano inicial da carga quando a base está vazia (app.importacao.ano-inicial).
	private final int anoInicial;

	// Impede que duas importações rodem ao mesmo tempo
	private final AtomicBoolean emExecucao = new AtomicBoolean(false);

	public ImportacaoService(DelegaciasRepository delegaciasRepository,
							 NaturezaRepository naturezaRepository,
							 OcorrenciaRepository ocorrenciaRepository,
							 SSPClient sspClient,
							 @Value("${app.importacao.ano-inicial:2020}") int anoInicial) {
		this.delegaciasRepository = delegaciasRepository;
		this.naturezaRepository = naturezaRepository;
		this.ocorrenciaRepository = ocorrenciaRepository;
		this.sspClient = sspClient;
		this.anoInicial = anoInicial;
	}


	public boolean dispararEmSegundoPlano() {
		if (!emExecucao.compareAndSet(false, true)) {
			return false;
		}

		Thread thread = new Thread(() -> {
			try {
				importarDadosSSP();
			} catch (Exception e) {
				log.error("Importação interrompida por erro inesperado: {}", e.getMessage(), e);
			} finally {
				emExecucao.set(false);
			}
		}, "importacao-ssp");

		thread.setDaemon(true);
		thread.start();
		return true;
	}

	public boolean isEmExecucao() {
		return emExecucao.get();
	}

	//Primeiro, realizar uma busca em nosso banco para ver o último mês/ano que foi salvo, e o mês/ano atual
	//Obs: A API SSP só disponibiliza os dados de 2 meses atrás em diante
	private List<YearMonth> obterMesesPendentes(long idDelegacia) {
		LocalDate ultimaData = ocorrenciaRepository.buscarUltimaDataImportadaPorDelegacia(idDelegacia);

		YearMonth mesAtual = YearMonth.now().minusMonths(2);

		List<YearMonth> mesesPendentes = new ArrayList<>();

		// Se banco estiver vazio, preencher todas as datas primeiro
		if (ultimaData == null) {
			int year = anoInicial;
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

		List<Delegacias> listaDelegacias = delegaciasRepository.findAll();

		int total = listaDelegacias.size();
		int processadas = 0;
		long inicio = System.currentTimeMillis();

		log.info("Importação iniciada: {} delegacias, ano inicial {}", total, anoInicial);

		for(Delegacias delegacia : listaDelegacias) {
			processadas++;
			try {
				List<YearMonth> meses = obterMesesPendentes(delegacia.getId());
				if(meses.isEmpty()) {
					log.info("[{}/{}] {} — já em dia", processadas, total, delegacia.getDelegacia());
					continue;
				}

				List<Ocorrencia> dados  = sspClient.buscarOcorrencias(meses, delegacia.getIdSSP());

				ocorrenciaRepository.saveAll(dados);

				log.info("[{}/{}] {} — {} meses, {} registros importados",
						processadas, total, delegacia.getDelegacia(), meses.size(), dados.size());
			} catch (Exception e) {
				log.error("[{}/{}] Erro ao importar dados da delegacia [{} - {}]: {}",
						processadas, total, delegacia.getId(), delegacia.getDelegacia(), e.getMessage(), e);
			}
		}

		log.info("Importação concluída em {} min ({} de {} delegacias processadas)",
				(System.currentTimeMillis() - inicio) / 60000, processadas, total);
	}


}
