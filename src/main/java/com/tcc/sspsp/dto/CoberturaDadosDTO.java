package com.tcc.sspsp.dto;

public record CoberturaDadosDTO(
        String primeiroMesDisponivel,
        String ultimoMesDisponivel,
        Long totalOcorrenciasRegistradas
) {}