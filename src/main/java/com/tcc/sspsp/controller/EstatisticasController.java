package com.tcc.sspsp.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcc.sspsp.dto.MediaOcorrenciasDTO;
import com.tcc.sspsp.dto.PrevisaoOcorrenciasDTO;
import com.tcc.sspsp.dto.TendenciaOcorrenciaDTO;
import com.tcc.sspsp.service.EstatisticaService;

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
    public MediaOcorrenciasDTO MediaMensal(
        @RequestParam(required = false) String tipo,
        @RequestParam(required = true) int ano
    ){
        
    Double media = service.calcularMediaMensal(tipo, ano);
  
    return new MediaOcorrenciasDTO(tipo, media);
        }
    //Previsao das ocorrencias
        @GetMapping("/previsao")
    public PrevisaoOcorrenciasDTO PrevisaoOcorrencia(
        @RequestParam(required = false) String tipo,
        @RequestParam(required = true) int ano)
    {
        PrevisaoOcorrenciasDTO response = service.calcularPrevisao(tipo, ano);

        return response;
    }
    // Tendencia de ocorrencias (Se está subindo, descendo ou estável)
    @GetMapping("/tendencia")
    public TendenciaOcorrenciaDTO TendenciaOcorrencia(
           @RequestParam(required = false) String tipo,
        @RequestParam(required = true) int ano)
    {
        TendenciaOcorrenciaDTO response = service.calcularTendencia(tipo, ano);

        return response;
    }
}
