package com.tcc.sspsp.dto;
 
public record RankingDelegaciaDTO(
    Long delegaciaId,
    String delegacia,
    String regiao,
    Long total
) {}