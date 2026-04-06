package client;

import java.io.InputStream;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import com.tcc.sspsp.model.Ocorrencia;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SSPClient {

    private static final String BASE_URL = "https://www.ssp.sp.gov.br/v1/OcorrenciasMensais/ExportarMensal";

    
    public List<Ocorrencia> buscarOcorrencias(List<YearMonth> meses, int idGrupo) {

    	Map<Integer, List<YearMonth>> mesesPorAno = meses.stream()
    	        .collect(Collectors.groupingBy(YearMonth::getYear));

    	    List<Ocorrencia> resultado = new ArrayList<>();

    	    for (Map.Entry<Integer, List<YearMonth>> entry : mesesPorAno.entrySet()) {

    	        Integer ano = entry.getKey();
    	        List<YearMonth> mesesDoAno = entry.getValue();

    	        InputStream excel = baixarExcel(ano, idGrupo);
    	        // OBS: TESTAR PARA VER SE ELE NÃO ACABA DANDO LIMITE DE REQUISIÇÃO
    	        List<Ocorrencia> dados = tratarExcel(excel, mesesDoAno);

    	        resultado.addAll(dados);
    	    }

    	    return resultado;
    }
    
    private List<Ocorrencia> tratarExcel(InputStream inputStream, List<YearMonth> mesesFiltrar) {

    	List<Ocorrencia> lista = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; 

                String natureza = getString(row.getCell(0));

                for (int i = 1; i <= 12; i++) {

                    int mes = i;

                    boolean mesDesejado = mesesFiltrar.stream()
                            .anyMatch(m -> m.getMonthValue() == mes);

                    if (!mesDesejado) continue;

                    int quantidade = getInt(row.getCell(i));


                    Ocorrencia ocorrencia = new Ocorrencia();
                   //Implementar: SETAR ATRIBUTOS DA OCORRENCIA, FALTA BUSCAR O ID DA NATUREZA NO BANCO.
                   

                    lista.add(ocorrencia);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler Excel", e);
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

    		    return new RestTemplate().getForObject(url, InputStream.class);
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