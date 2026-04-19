package com.tcc.sspsp.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tcc.sspsp.model.Ocorrencia;

@SpringBootTest
class SSPClientTest {

	@Autowired
	SSPClient sspclient;
	
	@Test
	void verificarLimiteRequisição() {
		
		List<YearMonth> yearMonth = new ArrayList<>();
		yearMonth.add(YearMonth.of(2024, 1));
		int i = 0;
		while(true) {
			i++;
			try {
				sspclient.buscarOcorrencias(yearMonth, 1275);
			} catch(Exception e) {
				System.out.println("Erro: " + e.getMessage() + " Número de requisições: " + i);
				break;
			}
		}
		
		
	}
}
