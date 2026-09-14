package com.tcc.sspsp.dto;
 
public record TotalNaturezaDTO(
    Long naturezaId,
    String natureza,
    Integer ano,
    Long total
) {}