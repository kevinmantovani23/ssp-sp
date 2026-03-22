package com.tcc.sspsp.dto;
 
public record OcorrenciaFiltroDTO(
    Integer ano,
    Long naturezaId,
    Long delegaciaId,
    int page,
    int size
) {}
 