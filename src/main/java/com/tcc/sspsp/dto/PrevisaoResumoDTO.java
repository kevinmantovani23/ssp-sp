package com.tcc.sspsp.dto;

public record PrevisaoResumoDTO(
	    String natureza,
	    String periodoUtilizado,
	    Long previsao,
	    String tendencia
	) {
	    public static PrevisaoResumoDTO semDados(String natureza) {
	        return new PrevisaoResumoDTO(natureza, null, null, "Sem dados");
	    }
	}
	 