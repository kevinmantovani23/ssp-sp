package com.tcc.sspsp.dto;
 
import java.time.LocalDate;
 
public record OcorrenciaResponseDTO(
    Long id,
    String natureza,
    String caracteristica,
    String delegacia,
    String regiao,
    Integer quantidade,
    LocalDate data
) {}