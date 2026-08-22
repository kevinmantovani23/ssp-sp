package com.tcc.sspsp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

public record OcorrenciaFiltroDTO(
    Integer ano,
    Long naturezaId,
    Long delegaciaId,
    @PositiveOrZero(message = "page não pode ser negativo") Integer page,
    @Max(value = 100, message = "size não pode ser maior que 100") Integer size
) {
    public OcorrenciaFiltroDTO {
        if (page == null) page = 0;
        if (size == null) size = 20;
    }
}
