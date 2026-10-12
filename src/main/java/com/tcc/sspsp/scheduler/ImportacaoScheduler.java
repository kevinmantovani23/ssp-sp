package com.tcc.sspsp.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tcc.sspsp.service.ImportacaoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportacaoScheduler {

	private final ImportacaoService importacaoService;
	
	// Defina app.importacao.cron como "-" para desabilitar o agendamento (ex.: em desenvolvimento).
	@Scheduled(cron = "${app.importacao.cron:0 0 3 * * *}")
	public void executar() {
	    if (!importacaoService.dispararEmSegundoPlano()) {
	        log.warn("Importação agendada ignorada: já existe uma importação em andamento.");
	    }
	}
	
}
