package com.tcc.sspsp.controller;

import com.tcc.sspsp.dto.*;
import com.tcc.sspsp.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/ocorrencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // em prod, restringir para o domínio do frontend
@Tag(name = "Ocorrências", description = "Endpoints de ocorrências policiais da SSP-SP")
public class OcorrenciaController {

    private final OcorrenciaService service;

    // ── GET /v1/ocorrencias ───────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary = "Lista ocorrências com filtros e paginação",
        description = "Retorna ocorrências filtradas por ano, natureza e delegacia. Suporta paginação."
    )
    public ResponseEntity<ApiResponseDTO<Page<OcorrenciaResponseDTO>>> listar(
        @Parameter(description = "Ano da ocorrência (ex: 2023)")
        @RequestParam(required = false) Integer ano,

        @Parameter(description = "ID da natureza da ocorrência")
        @RequestParam(required = false) Long naturezaId,

        @Parameter(description = "ID da delegacia")
        @RequestParam(required = false) Long delegaciaId,

        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var filtro = new OcorrenciaFiltroDTO(ano, naturezaId, delegaciaId, page, size);
        return ResponseEntity.ok(ApiResponseDTO.ok(service.listarComFiltros(filtro)));
    }

    // ── GET /v1/ocorrencias/totais ────────────────────────────────────────
    @GetMapping("/totais")
    @Operation(
        summary = "Totais por natureza e ano",
        description = "Agrupa o total de ocorrências por tipo/natureza. Ideal para gráficos de barras."
    )
    public ResponseEntity<ApiResponseDTO<List<TotalNaturezaDTO>>> totaisPorNatureza(
        @Parameter(description = "Filtrar por ano específico (opcional)")
        @RequestParam(required = false) Integer ano
    ) {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.totalPorNatureza(ano)));
    }

    // ── GET /v1/ocorrencias/serie-historica ───────────────────────────────
    @GetMapping("/serie-historica")
    @Operation(
        summary = "Série histórica mensal",
        description = "Retorna a evolução mensal de uma natureza de ocorrência. Ideal para gráficos de linha."
    )
    public ResponseEntity<ApiResponseDTO<List<SerieHistoricaDTO>>> serieHistorica(
        @Parameter(description = "ID da natureza", required = true)
        @RequestParam Long naturezaId,

        @Parameter(description = "Ano de início do período")
        @RequestParam(defaultValue = "2020") int anoInicio,

        @Parameter(description = "Ano de fim do período")
        @RequestParam(defaultValue = "2024") int anoFim
    ) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
            service.serieHistorica(naturezaId, anoInicio, anoFim)
        ));
    }

    // ── GET /v1/ocorrencias/ranking-delegacias ────────────────────────────
    @GetMapping("/ranking-delegacias")
    @Operation(
        summary = "Ranking de delegacias por ocorrências",
        description = "Lista as delegacias ordenadas pelo total de ocorrências (descendente)."
    )
    public ResponseEntity<ApiResponseDTO<List<RankingDelegaciaDTO>>> rankingDelegacias(
        @Parameter(description = "Filtrar por ano específico (opcional)")
        @RequestParam(required = false) Integer ano
    ) {
        return ResponseEntity.ok(ApiResponseDTO.ok(service.rankingDelegacias(ano)));
    }
}