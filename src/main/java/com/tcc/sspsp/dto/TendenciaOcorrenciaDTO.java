package com.tcc.sspsp.dto;

public record TendenciaOcorrenciaDTO(
    String tendencia,
    Double valor
)

{
    public static TendenciaOcorrenciaDTO semDados() {
        return new TendenciaOcorrenciaDTO("Sem dados", null);
    }
}