package com.tcc.sspsp.controller;


import com.tcc.sspsp.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcc.sspsp.service.EstatisticaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/estatisticas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Estatistica", description = "Estatisticas das ocorrências em SSP-SP")
public class EstatisticasController {
	
	private final EstatisticaService service;
	
    //Média mensal de cada tipo de ocorrência
    @GetMapping("/media-mensal")
    @Operation(
            summary = "Média mensal das ocorrências",
            description = "Calcula  a média mensal das ocorrências em um ano, podendo ser filtrado por Natureza, Natureza e Região" +
                    ", Natureza e Delegacia. Delegacia e Região não são permitidas juntas."
    )
    public ResponseEntity<ApiResponseDTO<MediaOcorrenciasDTO>> mediaMensal(
        @Parameter(description = "Natureza das ocorrências", required = true)
        @RequestParam(required = true) Long naturezaId,
        @Parameter(description = "Ano das ocorrências", required = true)
        @RequestParam(required = true) int ano,
        @Parameter(description = "ID da delegacia da ocorrência", required = false)
        @RequestParam(required = false) Long delegaciaId,
        @Parameter(description = "Região da ocorrência", required = false)
        @RequestParam(required = false) String regiao)
    {

	    return ResponseEntity.ok(ApiResponseDTO.ok(service.calcularMediaMensal(naturezaId, ano, delegaciaId, regiao)));
    }
    
    
    //Previsao das ocorrencias — calcula automaticamente o próximo mês após o último mês disponível na base
    @GetMapping("/previsao")
    @Operation(
        summary = "Previsão de ocorrências para o próximo mês",
        description = "Usa o histórico disponível nos últimos 4 anos para prever o mês " +
                "seguinte, podendo ser filtrado por Natureza, Natureza e Região" +
                ", Natureza e Delegacia. Delegacia e Região não são permitidas juntas."
    )
    public ResponseEntity<ApiResponseDTO<PrevisaoResumoDTO>> previsaoOcorrencia(
        @Parameter(description = "ID da natureza da ocorrência", required = true)
        @RequestParam Long naturezaId,
        @Parameter(description = "ID da delegacia da ocorrência", required = false)
        @RequestParam(required = false) Long delegaciaId,
        @Parameter(description = "Região da ocorrência", required = false)
        @RequestParam(required = false) String regiao)
    {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.calcularPrevisao(naturezaId, delegaciaId, regiao)));
    }
    
    
    // Tendencia de ocorrencias (Se está subindo, descendo ou estável)
    @GetMapping("/tendencia")
    @Operation(
            summary = "Tendências de ocorrencias, se está subindo, descendo ou estável",
            description = "Usa o histórico disponível das ocorrências nos últimos 4 anos," +
                    " podendo ser filtrado por Natureza, Natureza e Região" +
                    ", Natureza e Delegacia. Delegacia e Região não são permitidas juntas."
    )
    public ResponseEntity<ApiResponseDTO<TendenciaOcorrenciaDTO>> tendenciaOcorrencia(
        @Parameter(description = "ID da natureza da ocorrência", required = true)
        @RequestParam Long naturezaId,
        @Parameter(description = "ID da delegacia da ocorrência", required = false)
        @RequestParam(required = false) Long delegaciaId,
        @Parameter(description = "Região da ocorrência", required = false)
        @RequestParam(required = false) String regiao)
    {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.calcularTendencia(naturezaId, delegaciaId, regiao)));

    }
}
