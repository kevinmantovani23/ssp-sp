package com.tcc.sspsp.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginaDTO<T>(
        List<T> conteudo, int pagina, int tamanho,
        long totalElementos, int totalPaginas, boolean ultima
) {
    public static <T> PaginaDTO<T> de(Page<T> page) {
        return new PaginaDTO<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
