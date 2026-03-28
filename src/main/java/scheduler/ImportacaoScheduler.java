package scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tcc.sspsp.service.ImportacaoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImportacaoScheduler {

	private final ImportacaoService importacaoService;
	
	@Scheduled(cron = "0 0 3 * * *")
	public void executar() {
	    importacaoService.importar();
	}
	
}
