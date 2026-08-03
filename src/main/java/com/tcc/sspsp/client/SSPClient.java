package com.tcc.sspsp.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.ArrayList;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tcc.sspsp.model.Delegacias;
import com.tcc.sspsp.model.Natureza;
import com.tcc.sspsp.model.Ocorrencia;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.repository.NaturezaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class SSPClient {

    private static final String BASE_URL = "https://www.ssp.sp.gov.br/v1/OcorrenciasMensais/ExportarMensal";
    private static final int MAX_TENTATIVAS = 5;
    private static final long ESPERA_PREVENTIVA_MS = 500;
    private static final long ESPERA_BASE_BACKOFF_MS = 2000;
    private final NaturezaRepository naturezaRepository;
    private final DelegaciasRepository delegaciasRepository;
    private final RestTemplate restTemplate;

    public List<Ocorrencia> buscarOcorrencias(List<YearMonth> meses, int idGrupo) {

        Map<Integer, List<YearMonth>> mesesPorAno = meses.stream()
                .collect(Collectors.groupingBy(YearMonth::getYear));

        List<Ocorrencia> resultado = new ArrayList<>();

        for (Map.Entry<Integer, List<YearMonth>> entry : mesesPorAno.entrySet()) {

            Integer ano = entry.getKey();
            List<YearMonth> mesesDoAno = entry.getValue();

            InputStream excel = baixarExcel(ano, idGrupo);
            // OBS: TESTAR PARA VER SE ELE NÃO ACABA DANDO LIMITE DE REQUISIÇÃO
            Delegacias delegacia = delegaciasRepository.findByIdSSP(idGrupo)
                    .orElseThrow(() -> new EntityNotFoundException("Delegacia não encontrada para idSSP: " + idGrupo));
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

            log.error("Erro ao ler Excel: {}", e.getMessage(), e);

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

        aguardar(ESPERA_PREVENTIVA_MS);

        ResponseEntity<byte[]> response = executarComRetry(url);

        if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erro ao baixar Excel: resposta vazia HTTP CODE: " + response.getStatusCode());
        }

        return new ByteArrayInputStream(response.getBody());
    }

    private ResponseEntity<byte[]> executarComRetry(String url) {
        int tentativa = 0;

        while (true) {
            try {
                return restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                tentativa++;
                if (tentativa >= MAX_TENTATIVAS) {
                    throw new RuntimeException("Limite de requisições da SSP excedido após " + MAX_TENTATIVAS + " tentativas", e);
                }

                long espera = calcularEspera(e, tentativa);
                log.warn("Rate limit da SSP (429). Tentativa {}/{}, aguardando {}ms", tentativa, MAX_TENTATIVAS, espera);
                aguardar(espera);
            }
        }
    }

    private long calcularEspera(HttpClientErrorException.TooManyRequests e, int tentativa) {
        String retryAfter = e.getResponseHeaders() != null ? e.getResponseHeaders().getFirst("Retry-After") : null;
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter) * 1000;
            } catch (NumberFormatException ne) {
            }
        }
        return ESPERA_BASE_BACKOFF_MS * (1L << (tentativa - 1));
    }

    private void aguardar(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Importação interrompida durante espera", e);
        }
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