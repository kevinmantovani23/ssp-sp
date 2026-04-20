package com.tcc.sspsp.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tcc.sspsp.model.Delegacias;
import com.tcc.sspsp.model.Natureza;
import com.tcc.sspsp.model.Ocorrencia;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@AllArgsConstructor
public class SSPClient {

    private static final String BASE_URL = "https://www.ssp.sp.gov.br/v1/OcorrenciasMensais/ExportarMensal";
    private NaturezaRepository naturezaRepository;
    private DelegaciasRepository delegaciasRepository;
    
    public List<Ocorrencia> buscarOcorrencias(List<YearMonth> meses, int idGrupo) {

    	Map<Integer, List<YearMonth>> mesesPorAno = meses.stream()
    	        .collect(Collectors.groupingBy(YearMonth::getYear));

    	    List<Ocorrencia> resultado = new ArrayList<>();

    	    for (Map.Entry<Integer, List<YearMonth>> entry : mesesPorAno.entrySet()) {

    	        Integer ano = entry.getKey();
    	        List<YearMonth> mesesDoAno = entry.getValue();

    	        InputStream excel = baixarExcel(ano, idGrupo);
    	        // OBS: TESTAR PARA VER SE ELE NÃO ACABA DANDO LIMITE DE REQUISIÇÃO
    	        Optional<Delegacias> delegaciaOptional = delegaciasRepository.findByIdSSP(idGrupo);
    	        Delegacias delegacia = delegaciaOptional.get();
    	        List<Ocorrencia> dados = tratarExcel(excel, mesesDoAno, delegacia);

    	        resultado.addAll(dados);
    	    }

    	    return resultado;
    }
    
    private List<Ocorrencia> tratarExcel(InputStream inputStream, List<YearMonth> mesesFiltrar, Delegacias delegacia) {

    	List<Ocorrencia> lista = new ArrayList<>();
    	Map<String, Natureza> cacheNatureza = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; 

                String nomeNatureza = getString(row.getCell(0));
                
                //Buscar a natureza no banco pelo nome aqui, se não existir criar o registro lá.
                Natureza natureza = buscarOuCriarNatureza(nomeNatureza, cacheNatureza);

                for (int i = 1; i <= 12; i++) {

                    int mes = i;

                    Optional<YearMonth> mesEncontrado = mesesFiltrar.stream()
                    	    .filter(m -> m.getMonthValue() == mes)
                    	    .findFirst();

                    if (mesEncontrado.isEmpty()) continue;
                    
                    YearMonth yearMonthAtual = mesEncontrado.get();
                    
                    int quantidade = getInt(row.getCell(i));


                    Ocorrencia ocorrencia = new Ocorrencia();
                    
                   ocorrencia.setData(yearMonthAtual.atDay(1));
                   ocorrencia.setDelegacia(delegacia);
                   ocorrencia.setNatureza(natureza);
                   ocorrencia.setQuantidade(quantidade);
                   lista.add(ocorrencia);
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao ler Excel: " + e.getMessage());
        }     

        return lista;
    }
    
    private InputStream baixarExcel(int ano, int idGrupo) {
    	
    	String url = UriComponentsBuilder.fromUriString(BASE_URL)
    		        .queryParam("ano", ano)
    		        .queryParam("grupoDelito", 6)
    		        .queryParam("tipoGrupo", "DISTRITO")
    		        .queryParam("idGrupo", idGrupo)
    		        .toUriString();
    	 try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
    	 
    	RestTemplate restTemplate = new RestTemplate();

	    ResponseEntity<byte[]> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            null,
	            byte[].class
	    );

	    if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful()) {
	        throw new RuntimeException("Erro ao baixar Excel: resposta vazia HTTP CODE: " + response.getStatusCode());
	    }
	    
	    return new ByteArrayInputStream(response.getBody());
    }
    
    private Natureza buscarOuCriarNatureza(String nome, Map<String, Natureza> cache) {

        String nomeNormalizado = nome.trim().toUpperCase();

        
		return cache.computeIfAbsent(nomeNormalizado, n ->
            naturezaRepository.findByNatureza(n)
                .orElseGet(() -> {
                    Natureza nova = new Natureza();
                    nova.setNatureza(n);
                    nova.setCaracteristica("");
                    return naturezaRepository.save(nova);
                })
        );
    }
    
    private String getString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };
    }
    
    private int getInt(Cell cell) {
        if (cell == null) return 0;

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                String valor = cell.getStringCellValue().replace(".", "").trim();
                yield valor.isEmpty() ? 0 : Integer.parseInt(valor);
            }
            default -> 0;
        };
    }
    
}