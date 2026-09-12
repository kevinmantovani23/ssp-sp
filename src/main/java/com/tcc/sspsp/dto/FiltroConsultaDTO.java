package com.tcc.sspsp.dto;

import com.tcc.sspsp.utils.NormalizaCampos;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FiltroConsultaDTO(
        @NotNull Long naturezaId,
        Long delegaciaId,
        String regiao,
        @Min(2001) Integer anoInicio,
        @Min(2001) Integer anoFim
) {
    public FiltroConsultaDTO {
        if (delegaciaId != null && regiao != null)
            throw new IllegalArgumentException("Informe delegaciaId ou regiao, não ambos.");
        if (anoInicio != null && anoFim != null && anoInicio > anoFim)
            throw new IllegalArgumentException("anoInicio não pode ser maior que anoFim.");
        regiao = NormalizaCampos.normalizaRegiao(regiao);
    }
}