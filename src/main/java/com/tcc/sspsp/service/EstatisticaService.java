package com.tcc.sspsp.service;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcc.sspsp.repository.OcorrenciaRepository;
import com.tcc.sspsp.dto.PrevisaoOcorrenciasDTO;
import com.tcc.sspsp.dto.TendenciaOcorrenciaDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstatisticaService {

	private final OcorrenciaRepository repo;

    public Double calcularMediaMensal(String natureza, int ano){
    	List<Long> totais = repo.buscarTotaisMensais(natureza, ano);

        return totais.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    private List<PrevisaoOcorrenciasDTO> buscarSerieMensal(String natureza, int ano) {
    	
        return repo.calcularPrevisao(natureza, ano).stream()
            .map(row -> new PrevisaoOcorrenciasDTO(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue()
            ))
            .toList();
    }

        // Regressão Linear de previsão de ocorrencias para o proximo mes (Dados oscilam muito mas ao menos pega uma ideia geral de previsão)
    public PrevisaoOcorrenciasDTO calcularPrevisao(String natureza, int ano){
    	
        List<PrevisaoOcorrenciasDTO> lista = buscarSerieMensal(natureza, ano);

        SimpleRegression regression = new SimpleRegression();
        
        Integer mes = 1;

        for (PrevisaoOcorrenciasDTO o : lista){
            System.out.println(mes + ", " + o.total());
            regression.addData(mes, o.total());
            mes++;
        
        }
        System.out.println(regression.getSlope());
        Long previsao = (Long)Math.round(regression.predict(mes));
        return new PrevisaoOcorrenciasDTO(
            mes,
            previsao
        );
    }
    //Tendencia de ocorrencias
    public TendenciaOcorrenciaDTO calcularTendencia(String natureza, int ano) {
  
    	List<PrevisaoOcorrenciasDTO> lista = buscarSerieMensal(natureza, ano);

        SimpleRegression regression = new SimpleRegression();
        
        Integer mes = 1;

        for (PrevisaoOcorrenciasDTO o : lista){
            regression.addData(mes, o.total());
           // System.out.println(mes + ", " + o.total());
            mes++;
        
        }
      //  System.out.println(regression.getSlope());
        Double previsao = regression.getSlope();
        String tendencia;
        if (previsao > 0){
            tendencia = "crescimento";
        } else if (previsao == 0){
            tendencia = "estável";
        } else tendencia = "queda";

        return new TendenciaOcorrenciaDTO(
            tendencia,
            previsao
        );
        
    }
    
}